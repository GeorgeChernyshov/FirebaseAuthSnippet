package com.example.firebaseauthsnippet.screen

import android.content.IntentSender
import com.google.firebase.auth.FirebaseUser

data class FirebaseAuthScreenUiState(
    val user: FirebaseUser?,
    val isLoading: Boolean,
    val error: String?,
    val signInIntentSender: IntentSender?,
    val isAuthenticated: Boolean,
    val emailInput: String?,
    val passwordInput: String?
) {
    companion object {
        val DEFAULT = FirebaseAuthScreenUiState(
            user = null,
            isLoading = false,
            error = null,
            signInIntentSender = null,
            isAuthenticated = false,
            emailInput = null,
            passwordInput = null
        )
    }
}