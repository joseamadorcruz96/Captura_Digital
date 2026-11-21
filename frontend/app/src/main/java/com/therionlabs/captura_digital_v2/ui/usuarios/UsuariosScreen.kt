package com.therionlabs.captura_digital_v2.ui.usuarios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.therionlabs.captura_digital_v2.data.model.User
import com.therionlabs.captura_digital_v2.data.model.UserRole
import com.therionlabs.captura_digital_v2.ui.components.DuocButton
import com.therionlabs.captura_digital_v2.ui.components.DuocCard
import com.therionlabs.captura_digital_v2.ui.components.DuocHeader
import com.therionlabs.captura_digital_v2.ui.components.DuocTextField
import com.therionlabs.captura_digital_v2.ui.theme.DuocAmarillo
import com.therionlabs.captura_digital_v2.ui.theme.DuocAzul
import com.therionlabs.captura_digital_v2.ui.theme.ExitoVerde
import com.therionlabs.captura_digital_v2.ui.theme.FondoClaro

@Composable
fun UsuariosRoute(
    viewModel: UsuariosViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    UsuariosScreen(
        uiState = uiState,
        onNombreChange = viewModel::onNombreChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onRolChange = viewModel::onRolChange,
        onCrear = viewModel::crear,
        onBack = onBack
    )
}

@Composable
fun UsuariosScreen(
    uiState: UsuariosUiState,
    onNombreChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRolChange: (UserRole) -> Unit,
    onCrear: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoClaro)
            .verticalScroll(rememberScrollState())
    ) {
        DuocHeader(
            title = "Usuarios",
            subtitle = "Crea las cuentas de los captadores",
            onBack = onBack
        )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DuocCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Nuevo usuario",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DuocAzul
                )
                Spacer(Modifier.height(12.dp))

                DuocTextField(
                    value = uiState.nombre,
                    onValueChange = onNombreChange,
                    label = "Nombre",
                    errorMessage = uiState.nombreError,
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
                Spacer(Modifier.height(12.dp))

                DuocTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    label = "Correo electrónico",
                    keyboardType = KeyboardType.Email,
                    errorMessage = uiState.emailError,
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
                )
                Spacer(Modifier.height(12.dp))

                DuocTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    label = "Contraseña",
                    isPassword = true,
                    keyboardType = KeyboardType.Password,
                    errorMessage = uiState.passwordError,
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
                )
                Spacer(Modifier.height(12.dp))

                Text("Rol", fontWeight = FontWeight.SemiBold, color = DuocAzul)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(UserRole.CAPTADOR, UserRole.ADMINISTRATIVO).forEach { rol ->
                        FilterChip(
                            selected = uiState.rol == rol,
                            onClick = { onRolChange(rol) },
                            label = { Text(rol.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DuocAmarillo,
                                selectedLabelColor = DuocAzul
                            )
                        )
                    }
                }

                uiState.error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                uiState.mensaje?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = ExitoVerde, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(16.dp))
                DuocButton(
                    text = "Crear usuario",
                    onClick = onCrear,
                    isLoading = uiState.cargando
                )
            }

            Text(
                "Usuarios registrados (${uiState.usuarios.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DuocAzul
            )

            uiState.usuarios.forEach { usuario ->
                UsuarioItem(usuario)
            }
        }
    }
}

@Composable
private fun UsuarioItem(usuario: User) {
    DuocCard(modifier = Modifier.fillMaxWidth(), elevation = 1.dp, contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = DuocAmarillo
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        usuario.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = DuocAzul
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(usuario.name, fontWeight = FontWeight.Bold, color = DuocAzul)
                Text(usuario.email, color = Color.DarkGray, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                usuario.role.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}