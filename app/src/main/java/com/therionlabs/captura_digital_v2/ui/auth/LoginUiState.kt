package com.therionlabs.captura_digital_v2.ui.auth

import com.therionlabs.captura_digital_v2.data.model.User

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val error: String? = null,
    val user: User? = null
)