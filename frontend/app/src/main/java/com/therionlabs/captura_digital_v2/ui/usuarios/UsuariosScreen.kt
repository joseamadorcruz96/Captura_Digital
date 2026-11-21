package com.therionlabs.captura_digital_v2.ui.usuarios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
        onGuardar = viewModel::guardar,
        onEditar = viewModel::editar,
        onCancelar = viewModel::cancelarEdicion,
        onEliminar = viewModel::eliminar,
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
    onGuardar: () -> Unit,
    onEditar: (User) -> Unit,
    onCancelar: () -> Unit,
    onEliminar: (Int) -> Unit,
    onBack: () -> Unit
) {
    val editando = uiState.editandoId != null
    var porEliminar by remember { mutableStateOf<User?>(null) }

    porEliminar?.let { usuario ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("Eliminar usuario") },
            text = { Text("¿Seguro que quieres eliminar a ${usuario.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    onEliminar(usuario.id)
                    porEliminar = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) {
                    Text("Cancelar", color = DuocAzul)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoClaro)
            .verticalScroll(rememberScrollState())
    ) {
        DuocHeader(
            title = "Usuarios",
            subtitle = "Gestiona las cuentas de los captadores",
            onBack = onBack
        )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DuocCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (editando) "Editar usuario" else "Nuevo usuario",
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
                    label = if (editando) "Nueva contraseña" else "Contraseña",
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
                    text = if (editando) "Guardar cambios" else "Crear usuario",
                    onClick = onGuardar,
                    isLoading = uiState.cargando
                )

                if (editando) {
                    TextButton(
                        onClick = onCancelar,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Cancelar", color = DuocAzul)
                    }
                }
            }

            Text(
                "Usuarios registrados (${uiState.usuarios.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DuocAzul
            )

            uiState.usuarios.forEach { usuario ->
                UsuarioItem(
                    usuario = usuario,
                    onEditar = { onEditar(usuario) },
                    onEliminar = { porEliminar = usuario }
                )
            }
        }
    }
}

@Composable
private fun UsuarioItem(
    usuario: User,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
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
                Text(usuario.role.displayName, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onEditar) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = DuocAzul)
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}