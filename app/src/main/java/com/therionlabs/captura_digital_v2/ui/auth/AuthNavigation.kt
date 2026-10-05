package com.therionlabs.captura_digital_v2.ui.auth

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.therionlabs.captura_digital_v2.ui.help.HelpScreen

@Composable
fun AuthNavigation(onLoginSuccess: () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginRoute(
                onLoginSuccess = {
                    onLoginSuccess()
                    navController.navigate("help") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onForgotPassword = { navController.navigate("recover") }
            )
        }

        composable("recover") {
            RecoverRoute(onBack = { navController.popBackStack() })
        }

        composable("help") {
            HelpScreen(
                onBack = {
                    navController.navigate("login") {
                        popUpTo("help") { inclusive = true }
                    }
                }
            )
        }
    }
}