package com.therionlabs.captura_digital_v2.ui.kiosco

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therionlabs.captura_digital_v2.data.model.AppMode
import com.therionlabs.captura_digital_v2.data.model.User
import com.therionlabs.captura_digital_v2.ui.auth.DuocAzul
import com.therionlabs.captura_digital_v2.ui.auth.FondoClaro
import com.therionlabs.captura_digital_v2.ui.components.DuocButton
import com.therionlabs.captura_digital_v2.ui.components.DuocCard
import com.therionlabs.captura_digital_v2.ui.components.DuocHeader
import com.therionlabs.captura_digital_v2.ui.components.DuocOutlinedButton

/**
 * Destino controlado para sesiones con rol [com.therionlabs.captura_digital_v2.data.model.UserRole.PROSPECTO]
 * hasta la implementación completa del Modo Kiosco en Épica 3.
 * Restringe acceso a Dashboard y gestión de Usuarios.
 */
@Composable
fun KioscoPlaceholderScreen(
    user: User?,
    eventName: String,
    appMode: AppMode,
    onOpenForm: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoClaro)
    ) {
        DuocHeader(
            title = "Modo Kiosco",
            subtitle = user?.name ?: "Auto-registro de Postulante",
            showConnectivityStatus = false,
            trailingAction = {
                TextButton(onClick = onLogout) {
                    Text("Salir", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DuocCard(elevation = 2.dp, contentPadding = 16.dp) {
                Text(
                    text = "EVENTO ACTIVO",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = eventName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DuocAzul
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Operando en ${appMode.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }

            DuocCard(elevation = 4.dp, contentPadding = 20.dp) {
                Text(
                    text = "Auto-registro de Postulantes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DuocAzul
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Esta sesión está restringida al rol Prospecto (sin acceso a Dashboard ni Usuarios). El flujo completo de Kiosco con bloqueo por PIN se habilitará en la Épica 3.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
                Spacer(Modifier.height(20.dp))
                DuocButton(
                    text = "Completar Cupón de Admisión",
                    onClick = onOpenForm
                )
            }

            Spacer(Modifier.weight(1f))

            DuocOutlinedButton(
                text = "Cerrar sesión",
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
