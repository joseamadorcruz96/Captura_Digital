package com.therionlabs.captura_digital_v2.ui.inicio

import com.therionlabs.captura_digital_v2.data.local.DemoSeedGenerator
import com.therionlabs.captura_digital_v2.data.model.AppMode
import com.therionlabs.captura_digital_v2.data.model.Evento
import com.therionlabs.captura_digital_v2.data.model.ResumenCierreTurno
import com.therionlabs.captura_digital_v2.data.model.TurnoEvento
import com.therionlabs.captura_digital_v2.data.model.User
import com.therionlabs.captura_digital_v2.data.model.UserRole

data class InicioUiState(
    val user: User? = null,
    val appMode: AppMode = AppMode.REMOTE_GATEWAY,
    val activeEventId: String = DemoSeedGenerator.DEFAULT_EVENT_ID,
    val activeEventName: String = DemoSeedGenerator.DEFAULT_EVENT_NAME,
    val activeEvent: Evento? = null,
    val availableEvents: List<Evento> = emptyList(),
    val activeTurno: TurnoEvento? = null,
    val eventCapturedCount: Int = 0,
    val turnoCapturedCount: Int = 0,
    val resumenCierreTurno: ResumenCierreTurno? = null,
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

    val currentGoal: Int
        get() = activeTurno?.metaTurno ?: activeEvent?.metaProspectos ?: 30

    val currentProgressCount: Int
        get() = if (activeTurno != null) turnoCapturedCount else eventCapturedCount

    val progressFraction: Float
        get() = if (currentGoal <= 0) 0f else (currentProgressCount.toFloat() / currentGoal.toFloat()).coerceIn(0f, 1f)
}
