package com.therionlabs.captura_digital_v2.ui.inicio

import com.therionlabs.captura_digital_v2.data.model.User
import com.therionlabs.captura_digital_v2.data.model.UserRole

data class InicioUiState(
    val user: User? = null,
    val activeEventName: String = "Feria Vocacional Duoc UC 2026",
    val pendingSyncCount: Int = 0,
    val totalCapturedToday: Int = 10,
    val isOnline: Boolean = true
) {
    val isCaptador: Boolean
        get() = user?.role == UserRole.CAPTADOR

    val isAdministrativo: Boolean
        get() = user?.role == UserRole.ADMINISTRATIVO
}
