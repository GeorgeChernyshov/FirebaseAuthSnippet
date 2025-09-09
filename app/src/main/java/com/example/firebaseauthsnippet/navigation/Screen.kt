package com.example.firebaseauthsnippet.navigation

import androidx.annotation.StringRes
import com.example.firebaseauthsnippet.R
import kotlinx.serialization.Serializable

@Serializable
sealed class Screen(val route: String, @StringRes val resourceId: Int) {

    @Serializable
    data object FirebaseAuth : Screen(
        route = "firebase_auth",
        resourceId = R.string.label_firebase_auth
    )
}