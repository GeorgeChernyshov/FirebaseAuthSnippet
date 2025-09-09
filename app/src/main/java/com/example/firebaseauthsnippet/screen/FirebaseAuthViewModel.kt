package com.example.firebaseauthsnippet.screen

import android.content.Context
import android.content.IntentSender
import android.util.Log
import androidx.activity.result.launch
import androidx.compose.animation.core.copy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebaseauthsnippet.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class FirebaseAuthViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(FirebaseAuthScreenUiState.DEFAULT)
    val uiState = _uiState.asStateFlow()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val oneTapClient: SignInClient = Identity.getSignInClient(applicationContext)

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _uiState.value = _uiState.value.copy(
                user = firebaseAuth.currentUser,
                isAuthenticated = firebaseAuth.currentUser != null,
                isLoading = false // Ensure loading is off after auth state change
            )

            Log.d(TAG, "Auth state changed. User: ${firebaseAuth.currentUser?.displayName}")
        }
    }

    fun setError(error: String) {
        _uiState.value = _uiState.value.copy(error = error)
    }

    fun setIntentSender(intentSender: IntentSender?) {
        _uiState.value = _uiState.value.copy(signInIntentSender = intentSender)
    }

    fun beginGoogleSignIn() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null,
            signInIntentSender = null
        )

        viewModelScope.launch {
            try {
                val beginSignInRequest = BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            .setServerClientId(applicationContext.getString(R.string.default_web_client_id))
                            .setFilterByAuthorizedAccounts(false)
                            .build()
                    )
                    .setAutoSelectEnabled(true)
                    .build()

                val result = oneTapClient.beginSignIn(beginSignInRequest).await()
                _uiState.value = _uiState.value.copy(
                    signInIntentSender = result.pendingIntent.intentSender,
                    isLoading = false
                )

                Log.d(TAG, "beginSignIn successful, intent sender ready.")
            } catch (e: ApiException) {
                Log.w(TAG, "beginSignIn failed (usually no accounts or user has opted out): ${e.statusCode}, ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Google Sign-in unavailable or no accounts."
                )
            } catch (e: Exception) {
                Log.e(TAG, "beginSignIn unexpected error: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun handleSignInResult(
        idToken: String?
    ) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        try {
            val firebaseCredential = GoogleAuthProvider.getCredential(
                idToken,
                null
            )

            val result = auth.signInWithCredential(firebaseCredential)
                .await()

            Log.d(TAG, "Firebase signInWithCredential successful. User: ${result.user?.displayName}")
            _uiState.value = _uiState.value.copy(
                user = result.user,
                isAuthenticated = true,
                isLoading = false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Firebase sign in with credential failed: ${e.message}", e)
            _uiState.value = _uiState.value.copy(
                error = e.message,
                isAuthenticated = false,
                isLoading = false
            )
        } finally {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun signOut() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            try {
                auth.signOut()
                Log.d(TAG, "User signed out successfully from Firebase.")
                _uiState.value = _uiState.value.copy(
                    user = null,
                    isAuthenticated = false
                )
            } catch (e: Exception) {
                Log.e(TAG, "Sign out failed: ${e.message}", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    companion object {
        const val TAG = "FirebaseAuthViewModel"
    }
}