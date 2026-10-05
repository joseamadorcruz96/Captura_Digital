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
import com.therionlabs.captura_digital_v2.ui.help.HelpScreen

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

        // Temporal: hasta que exista la pantalla de inicio de José
        composable("menu") {
            TempMenu(
                onHelp = { navController.navigate("help") },
                onDashboard = { navController.navigate("dashboard") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("menu") { inclusive = true }
                    }
                }
            )
        }

        composable("help") {
            HelpScreen(onBack = { navController.popBackStack() })
        }

        composable("dashboard") {
            DashboardRoute(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun TempMenu(
    onHelp: () -> Unit,
    onDashboard: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoClaro)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Menú de pruebas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = DuocAzul
        )
        Spacer(Modifier.height(24.dp))

        listOf(
            "¿Cómo funciona?" to onHelp,
            "Dashboard" to onDashboard
        ).forEach { (text, action) ->
            Button(
                onClick = action,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DuocAmarillo,
                    contentColor = DuocAzul
                ),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(text, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
        }

        TextButton(onClick = onLogout) {
            Text("Cerrar sesión", color = DuocAzul)
        }
    }
}