package com.therionlabs.captura_digital_v2.ui.usuarios

import com.therionlabs.captura_digital_v2.data.model.User
import com.therionlabs.captura_digital_v2.data.model.UserRole

data class UsuariosUiState(
    val usuarios: List<User> = emptyList(),
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val rol: UserRole = UserRole.CAPTADOR,
    val nombreError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val error: String? = null,
    val mensaje: String? = null,
    val cargando: Boolean = false
)