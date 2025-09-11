package com.example.firebaseauthsnippet.screen

import android.app.Activity.RESULT_OK
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.firebaseauthsnippet.R
import com.example.firebaseauthsnippet.components.AppBar
import com.example.firebaseauthsnippet.theme.FirebaseAuthSnippetTheme
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

@Composable
fun FirebaseAuthScreen(
    viewModel: FirebaseAuthViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val googleSignInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        try {
            if (result.resultCode == RESULT_OK) {
                val signInCredential = Identity.getSignInClient(context)
                    .getSignInCredentialFromIntent(result.data)

                signInCredential.googleIdToken?.let { idToken ->
                    Log.d(TAG, "GIS Sign-In Result: googleIdToken = $idToken")

                    viewModel.handleSignInResult(idToken)
                    Log.d(TAG, "GIS Sign-In successful, GoogleIdTokenCredential received.")
                }
            } else {
                Log.w(TAG, "GIS Sign-In canceled or failed with result code: ${result.resultCode}")
                viewModel.setError("Sign-in cancelled or failed.")
            }
        } catch (e: ApiException) {
            Log.e(TAG, "GIS Sign-In failed: ${e.statusCode} ${e.message}", e)
            viewModel.setError("Sign-in failed: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during GIS Sign-In: ${e.message}", e)
            viewModel.setError("Unexpected error: ${e.message}")
        }
    }

    LaunchedEffect(uiState.value.signInIntentSender) {
        uiState.value.signInIntentSender?.let { sender ->
            googleSignInLauncher.launch(
                IntentSenderRequest
                    .Builder(sender)
                    .build()
            )

            viewModel.setIntentSender(null)
        }
    }

    FirebaseAuthScreenContent(
        uiState = uiState.value,
        onSignInGoogleClick = { viewModel.beginGoogleSignIn() },
        onSignOutClick = { viewModel.signOut() },
        onEmailInputChange = { viewModel.setEmail(it) },
        onPasswordInputChange = { viewModel.setPassword(it) },
        onSignInEmailClick = { viewModel.signInWithEmailAndPassword() },
        onSignUpEmailClick = { viewModel.createUserWithEmailAndPassword() }
    )
}

@Composable
fun FirebaseAuthScreenContent(
    uiState: FirebaseAuthScreenUiState,
    onSignInGoogleClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onEmailInputChange: (String) -> Unit,
    onPasswordInputChange: (String) -> Unit,
    onSignInEmailClick: () -> Unit,
    onSignUpEmailClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        topBar = { AppBar(name = stringResource(R.string.label_firebase_auth)) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading...", style = MaterialTheme.typography.bodyLarge)
            } else if (uiState.isAuthenticated) {
                uiState.user?.let { user ->
                    Text(
                        text = "Welcome, ${user.displayName ?: user.email}!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "User ID: ${user.uid}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onSignOutClick,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Sign Out")
                    }
                }
            } else {
                Text(
                    text = "Sign in to continue",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Button(
                    onClick = onSignInGoogleClick,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Sign in with Google")
                }

                Spacer(modifier = Modifier.height(24.dp)) // Separator for different auth methods
                Text("OR", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(24.dp))


                // Email/Password Input Fields and Buttons (NEW)
                OutlinedTextField(
                    value = uiState.emailInput.orEmpty(),
                    onValueChange = onEmailInputChange,
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )

                OutlinedTextField(
                    value = uiState.passwordInput.orEmpty(),
                    onValueChange = onPasswordInputChange,
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(
                        onClick = onSignInEmailClick,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                    ) {
                        Text("Sign In")
                    }
                    Button(
                        onClick = onSignUpEmailClick,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    ) {
                        Text("Sign Up")
                    }
                }

                uiState.error?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FirebaseAuthScreenContentPreview() {
    FirebaseAuthSnippetTheme {
        FirebaseAuthScreenContent(
            uiState = FirebaseAuthScreenUiState.DEFAULT,
            onSignInGoogleClick = {},
            onSignOutClick = {},
            onEmailInputChange = {},
            onPasswordInputChange = {},
            onSignInEmailClick = {},
            onSignUpEmailClick = {}
        )
    }
}

private const val TAG = "FirebaseAuthScreen"