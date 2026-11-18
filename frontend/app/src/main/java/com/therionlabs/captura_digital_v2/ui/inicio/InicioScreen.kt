package com.therionlabs.captura_digital_v2.ui.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.therionlabs.captura_digital_v2.ui.components.DuocButton
import com.therionlabs.captura_digital_v2.ui.components.DuocCard
import com.therionlabs.captura_digital_v2.ui.components.DuocHeader
import com.therionlabs.captura_digital_v2.ui.components.DuocOutlinedButton
import androidx.compose.ui.tooling.preview.Preview
import com.therionlabs.captura_digital_v2.data.model.User
import com.therionlabs.captura_digital_v2.data.model.UserRole
import com.therionlabs.captura_digital_v2.ui.theme.Captura_Digital_v2Theme
import com.therionlabs.captura_digital_v2.ui.theme.DuocAmarillo
import com.therionlabs.captura_digital_v2.ui.theme.DuocAzul
import com.therionlabs.captura_digital_v2.ui.theme.DuocAzulMedio
import com.therionlabs.captura_digital_v2.ui.theme.ExitoVerde
import com.therionlabs.captura_digital_v2.ui.theme.FondoClaro

@Composable
fun InicioRoute(
    viewModel: InicioViewModel = viewModel(),
    onNewCapture: () -> Unit,
    onDashboard: () -> Unit,
    onHelp: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    InicioScreen(
        uiState = uiState,
        onNewCapture = onNewCapture,
        onDashboard = onDashboard,
        onHelp = onHelp,
        onLogout = {
            viewModel.logout()
            onLogout()
        }
    )
}

@Composable
fun InicioScreen(
    uiState: InicioUiState,
    onNewCapture: () -> Unit,
    onDashboard: () -> Unit,
    onHelp: () -> Unit,
    onLogout: () -> Unit
) {
    val userName = uiState.user?.name ?: "Usuario"
    val roleName = uiState.user?.role?.displayName ?: "Captura Digital"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoClaro)
            .verticalScroll(rememberScrollState())
    ) {
        // Encabezado institucional
        DuocHeader(
            title = "¡Hola, $userName!",
            subtitle = roleName,
            trailingAction = {
                TextButton(onClick = onLogout) {
                    Text("Salir", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de Contexto Operativo y Estado de Red
            DuocCard(
                elevation = 1.dp,
                contentPadding = 14.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "EVENTO ACTIVO",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = uiState.activeEventName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = DuocAzul
                        )
                    }

                    // Badge de conectividad
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE8F5E9),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(ExitoVerde, CircleShape)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "En línea",
                                style = MaterialTheme.typography.labelSmall,
                                color = ExitoVerde,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Tarjeta de Acción Principal (Diferenciada por Rol)
            if (uiState.isCaptador) {
                DuocCard(
                    elevation = 4.dp,
                    contentPadding = 20.dp
                ) {
                    Text(
                        text = "📝 Captura en Terreno",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DuocAzul
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Abre el cupón oficial de admisión de Duoc UC para registrar los datos del postulante con validación de RUT en vivo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                    Spacer(Modifier.height(20.dp))
                    DuocButton(
                        text = "Iniciar Nueva Captura",
                        onClick = onNewCapture
                    )
                }
            } else {
                DuocCard(
                    elevation = 4.dp,
                    contentPadding = 20.dp
                ) {
                    Text(
                        text = "📊 Panel Administrativo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DuocAzul
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Visualiza el consolidado de prospectos, escuelas más solicitadas, promedios de edad y estadísticas del equipo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                    Spacer(Modifier.height(20.dp))
                    DuocButton(
                        text = "Ir al Dashboard",
                        onClick = onDashboard
                    )
                }
            }

            // Métricas Rápidas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DuocCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = 14.dp
                ) {
                    Text(
                        text = "${uiState.totalCapturedToday}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = DuocAzul
                    )
                    Text(
                        text = if (uiState.isCaptador) "Mis capturas hoy" else "Total registradas",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                DuocCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = 14.dp
                ) {
                    Text(
                        text = if (uiState.pendingSyncCount == 0) "Al día" else "${uiState.pendingSyncCount}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.pendingSyncCount == 0) ExitoVerde else DuocAmarillo
                    )
                    Text(
                        text = "Sincronización",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // Opciones Secundarias
            Text(
                text = "Accesos Rápidos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DuocAzul,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Accesos rápidos según rol
            if (uiState.isCaptador) {
                QuickActionCard(
                    title = "Dashboard de Resultados",
                    description = "Revisa cuántas encuestas llevas y el desglose de intereses",
                    iconText = "📊",
                    onClick = onDashboard
                )
            } else {
                QuickActionCard(
                    title = "Formulario de Captura",
                    description = "Apoya en terreno ingresando prospectos de forma directa",
                    iconText = "📝",
                    onClick = onNewCapture
                )
            }

            QuickActionCard(
                title = "¿Cómo funciona?",
                description = "Guía paso a paso sobre el flujo de captura y modo offline",
                iconText = "ℹ️",
                onClick = onHelp
            )

            Spacer(Modifier.height(8.dp))

            // Botón de Cerrar Sesión
            DuocOutlinedButton(
                text = "Cerrar sesión",
                onClick = onLogout
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    description: String,
    iconText: String,
    onClick: () -> Unit
) {
    DuocCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = 2.dp,
        contentPadding = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = Color(0xFFF1F5F9)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = iconText, style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = DuocAzul
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
            Text("›", style = MaterialTheme.typography.headlineSmall, color = Color.Gray)
        }
    }
}

@Preview(name = "Pantalla Inicio - Modo Claro", showBackground = true, showSystemUi = true)
@Composable
fun InicioScreenPreview() {
    Captura_Digital_v2Theme {
        InicioScreen(
            uiState = InicioUiState(
                user = User(
                    id = 1,
                    name = "Juan Pérez",
                    email = "juan.perez@duocuc.cl",
                    role = UserRole.CAPTADOR
                ),
                activeEventName = "Feria Vocacional Duoc UC 2026",
                pendingSyncCount = 2,
                totalCapturedToday = 15,
                isOnline = true
            ),
            onNewCapture = {},
            onDashboard = {},
            onHelp = {},
            onLogout = {}
        )
    }
}
