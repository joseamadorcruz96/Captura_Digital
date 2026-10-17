package com.therionlabs.captura_digital_v2.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.therionlabs.captura_digital_v2.ui.dashboard.DashboardRoute
import com.therionlabs.captura_digital_v2.ui.formulario.FormularioRoute
import com.therionlabs.captura_digital_v2.ui.help.HelpScreen
import com.therionlabs.captura_digital_v2.ui.inicio.InicioRoute

@Composable
fun AuthNavigation(onLoginSuccess: () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginRoute(
                onLoginSuccess = {
                    onLoginSuccess()
                    navController.navigate("menu") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onForgotPassword = { navController.navigate("recover") }
            )
        }

        composable("recover") {
            RecoverRoute(onBack = { navController.popBackStack() })
        }

        // Pantalla de Inicio oficial (feature/Inicio)
        composable("menu") {
            InicioRoute(
                onNewCapture = { navController.navigate("formulario") },
                onDashboard = { navController.navigate("dashboard") },
                onHelp = { navController.navigate("help") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("menu") { inclusive = true }
                    }
                }
            )
        }

        // Pantalla de Captura del Cupón Duoc UC (feature/Formulario)
        composable("formulario") {
            FormularioRoute(onBack = { navController.popBackStack() })
        }

        composable("help") {
            HelpScreen(onBack = { navController.popBackStack() })
        }

        composable("dashboard") {
            DashboardRoute(onBack = { navController.popBackStack() })
        }
    }
}