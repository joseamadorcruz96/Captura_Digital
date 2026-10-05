package com.therionlabs.captura_digital_v2.ui.auth

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AuthNavigation(onLoginSuccess: () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginRoute(
                onLoginSuccess = onLoginSuccess,
                onForgotPassword = { navController.navigate("recover") }
            )
        }

        composable("recover") {
            RecoverRoute(
                onBack = { navController.popBackStack() }
            )
        }
    }
}