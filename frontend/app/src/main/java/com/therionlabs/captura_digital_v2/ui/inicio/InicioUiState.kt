package com.therionlabs.captura_digital_v2.ui.inicio

import com.therionlabs.captura_digital_v2.data.model.AppMode
import com.therionlabs.captura_digital_v2.data.model.User
import com.therionlabs.captura_digital_v2.data.model.UserRole

data class InicioUiState(
    val user: User? = null,
    val appMode: AppMode = AppMode.REMOTE_GATEWAY,
    val activeEventId: String = "EVT-DUOC-2026",
    val activeEventName: String = "Feria Vocacional Duoc UC 2026",
    val pendingSyncCount: Int = 0,
    val syncErrorCount: Int = 0,
    val totalCapturedToday: Int = 0,
    val isOnline: Boolean = true,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null
) {
    val isCaptador: Boolean
        get() = user?.role == UserRole.CAPTADOR

    val isAdministrativo: Boolean
        get() = user?.role == UserRole.ADMINISTRATIVO
}
