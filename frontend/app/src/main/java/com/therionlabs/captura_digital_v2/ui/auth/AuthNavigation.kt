package com.therionlabs.captura_digital_v2.ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.therionlabs.captura_digital_v2.CapturaDigitalApp
import com.therionlabs.captura_digital_v2.data.local.DemoSeedGenerator
import com.therionlabs.captura_digital_v2.data.model.AppMode
import com.therionlabs.captura_digital_v2.data.model.User
import com.therionlabs.captura_digital_v2.data.model.UserRole
import com.therionlabs.captura_digital_v2.data.repository.AuthRepository
import com.therionlabs.captura_digital_v2.data.repository.EventoRepository
import com.therionlabs.captura_digital_v2.ui.components.OfflineStatusBanner
import com.therionlabs.captura_digital_v2.ui.components.rememberIsOnline
import com.therionlabs.captura_digital_v2.ui.dashboard.DashboardRoute
import com.therionlabs.captura_digital_v2.ui.eventos.EventosRoute
import com.therionlabs.captura_digital_v2.ui.formularioModal.StepFormRoute
import com.therionlabs.captura_digital_v2.ui.help.HelpScreen
import com.therionlabs.captura_digital_v2.ui.inicio.InicioRoute
import com.therionlabs.captura_digital_v2.ui.kiosco.KioscoPlaceholderScreen
import com.therionlabs.captura_digital_v2.ui.usuarios.UsuariosRoute

private fun destinationForRole(role: UserRole): String = when (role) {
    UserRole.ADMINISTRATIVO,
    UserRole.CAPTADOR -> "menu"
    UserRole.PROSPECTO -> "kiosco"
}

@Composable
fun AuthNavigation(
    onLoginSuccess: () -> Unit,
    authRepository: AuthRepository? = remember { CapturaDigitalApp.containerOrNull()?.authRepository },
    eventoRepository: EventoRepository? = remember { CapturaDigitalApp.containerOrNull()?.eventoRepository }
) {
    val isOnline = rememberIsOnline()

    if (authRepository != null) {
        val isSessionLoaded by authRepository.isSessionLoaded.collectAsStateWithLifecycle()
        val sessionState by authRepository.sessionState.collectAsStateWithLifecycle()
        val activeEvent by if (eventoRepository != null) {
            eventoRepository.observeEventoActivo().collectAsStateWithLifecycle(initialValue = null)
        } else {
            remember { androidx.compose.runtime.mutableStateOf(null) }
        }

        if (!isSessionLoaded) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DuocAzul)
            }
            return
        }

        val initialDestination = remember(isSessionLoaded) {
            sessionState.user?.let { destinationForRole(it.role) } ?: "login"
        }

        AuthNavHost(
            startDestination = initialDestination,
            isOnline = isOnline,
            onLoginSuccess = onLoginSuccess,
            onKioscoLogout = { authRepository.logout() },
            sessionUser = sessionState.user,
            sessionAppMode = sessionState.appMode,
            activeEventName = activeEvent?.nombre ?: DemoSeedGenerator.DEFAULT_EVENT_NAME,
            activeEventId = activeEvent?.id ?: sessionState.activeEventId ?: DemoSeedGenerator.DEFAULT_EVENT_ID
        )
    } else {
        AuthNavHost(
            startDestination = "login",
            isOnline = isOnline,
            onLoginSuccess = onLoginSuccess,
            onKioscoLogout = {},
            sessionUser = null,
            sessionAppMode = AppMode.LOCAL_DEMO,
            activeEventName = DemoSeedGenerator.DEFAULT_EVENT_NAME,
            activeEventId = DemoSeedGenerator.DEFAULT_EVENT_ID
        )
    }
}

@Composable
private fun AuthNavHost(
    startDestination: String,
    isOnline: Boolean,
    onLoginSuccess: () -> Unit,
    onKioscoLogout: () -> Unit,
    sessionUser: User?,
    sessionAppMode: AppMode,
    activeEventName: String,
    activeEventId: String
) {
    val navController = rememberNavController()

    Column(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.weight(1f)
        ) {
            composable("login") {
                LoginRoute(
                    onLoginSuccess = { user ->
                        onLoginSuccess()
                        navController.navigate(destinationForRole(user.role)) {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onForgotPassword = { navController.navigate("recover") }
                )
            }

            composable("recover") {
                RecoverRoute(onBack = { navController.popBackStack() })
            }

            // Pantalla de Inicio oficial (CAPTADOR / ADMINISTRATIVO)
            composable("menu") {
                InicioRoute(
                    onNewCapture = { navController.navigate("formulario") },
                    onDashboard = { navController.navigate("dashboard") },
                    onManageEventos = {
                        if (sessionUser?.role == UserRole.ADMINISTRATIVO) {
                            navController.navigate("eventos")
                        }
                    },
                    onHelp = { navController.navigate("help") },
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo("menu") { inclusive = true }
                        }
                    }
                )
            }

            // Placeholder controlado para PROSPECTO hasta construir Modo Kiosco en Épica 3
            composable("kiosco") {
                KioscoPlaceholderScreen(
                    user = sessionUser,
                    eventName = activeEventName,
                    appMode = sessionAppMode,
                    onOpenForm = { navController.navigate("formulario") },
                    onLogout = {
                        onKioscoLogout()
                        navController.navigate("login") {
                            popUpTo("kiosco") { inclusive = true }
                        }
                    }
                )
            }

            // Pantalla de Captura Modal Stepper del Cupón Duoc UC
            composable("formulario") {
                StepFormRoute(
                    eventoId = activeEventId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("help") {
                HelpScreen(onBack = { navController.popBackStack() })
            }

            composable("dashboard") {
                DashboardRoute(
                    onBack = { navController.popBackStack() },
                    onUsuarios = { navController.navigate("usuarios") },
                    onEventos = {
                        if (sessionUser?.role == UserRole.ADMINISTRATIVO) {
                            navController.navigate("eventos")
                        }
                    }
                )
            }

            composable("usuarios") {
                UsuariosRoute(onBack = { navController.popBackStack() })
            }

            composable("eventos") {
                if (sessionUser?.role == UserRole.ADMINISTRATIVO) {
                    EventosRoute(onBack = { navController.popBackStack() })
                } else {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
        }
        OfflineStatusBanner(isOnline = isOnline)
    }
}
