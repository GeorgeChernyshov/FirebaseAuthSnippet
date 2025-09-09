package com.example.firebaseauthsnippet

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.firebaseauthsnippet.navigation.Screen
import com.example.firebaseauthsnippet.screen.FirebaseAuthScreen
import com.example.firebaseauthsnippet.theme.FirebaseAuthSnippetTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { App() }
    }

    @Composable
    fun App() {
        val navController = rememberNavController()

        FirebaseAuthSnippetTheme {
            NavHost(
                navController = navController,
                startDestination = Screen.FirebaseAuth
            ) {
                composable<Screen.FirebaseAuth> {
                    FirebaseAuthScreen()
                }
            }
        }
    }
}