package com.therionlabs.captura_digital_v2.ui.auth

data class RecoverUiState(
    val email: String = "",
    val error: String? = null,
    val sent: Boolean = false
)