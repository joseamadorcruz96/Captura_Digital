package com.therionlabs.captura_digital_v2.ui.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SyncStatus
import com.therionlabs.captura_digital_v2.data.model.UserRole
import com.therionlabs.captura_digital_v2.data.remote.ConnectivityObserver
import com.therionlabs.captura_digital_v2.data.remote.NetworkMonitor
import com.therionlabs.captura_digital_v2.data.repository.AuthRepository
import com.therionlabs.captura_digital_v2.data.repository.IProspectoRepository
import com.therionlabs.captura_digital_v2.data.repository.ProspectoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class InicioViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val prospectoRepository: IProspectoRepository = ProspectoRepository(),
    private val connectivityObserver: ConnectivityObserver = NetworkMonitor.observer()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        InicioUiState(
            user = authRepository.getCurrentUser(),
            appMode = authRepository.appMode,
            activeEventId = ProspectoRepository.activeEventId,
            activeEventName = ProspectoRepository.activeEventName,
            isOnline = connectivityObserver.isCurrentlyOnline()
        )
    )
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    private var latestProspectos: List<Prospecto> = emptyList()
    private var lastAutoSyncPendingIds: Set<String> = emptySet()

    init {
        viewModelScope.launch {
            authRepository.sessionState.collect { session ->
                _uiState.update { state ->
                    state.copy(
                        user = session.user ?: authRepository.getCurrentUser(),
                        appMode = session.appMode
                    )
                }
            }
        }

        viewModelScope.launch {
            prospectoRepository.observeProspectos().collect { prospectos ->
                latestProspectos = prospectos
                val currentUser = authRepository.getCurrentUser()
                val capturedCount = if (currentUser.role == UserRole.ADMINISTRATIVO) {
                    prospectos.size
                } else {
                    prospectos.count { it.captadorId == currentUser.id && isSameLocalDay(it.fechaCaptura) }
                }
                val pending = prospectos.count { it.syncStatus != SyncStatus.SYNCED }
                val syncErrors = prospectos.count { it.syncStatus == SyncStatus.SYNC_ERROR }

                _uiState.update { state ->
                    // Si entraron nuevos pendientes, limpiamos mensajes de éxito anteriores para no contradecir el contador
                    val clearStaleMessage = pending > 0 &&
                        !state.isSyncing &&
                        (state.syncMessage?.startsWith("Sincronización completada") == true ||
                            state.syncMessage?.startsWith("Todo al día") == true)
                    state.copy(
                        user = currentUser,
                        totalCapturedToday = capturedCount,
                        pendingSyncCount = pending,
                        syncErrorCount = syncErrors,
                        syncMessage = if (clearStaleMessage) null else state.syncMessage
                    )
                }
            }
        }

        // Observa cortes y recuperación de red en tiempo real (incluso si el usuario está AFK)
        viewModelScope.launch {
            var estabaOnline = connectivityObserver.isCurrentlyOnline()
            connectivityObserver.isOnline.collect { online ->
                _uiState.update { it.copy(isOnline = online) }
                if (!estabaOnline && online) {
                    lastAutoSyncPendingIds = emptySet()
                    sincronizarYRefrescarSiHayRed()
                }
                estabaOnline = online
            }
        }
    }

    /**
     * Refresca evento activo y datos remotos al entrar o volver a Inicio,
     * auto-sincronizando registros PENDING_SYNC si hay conectividad.
     */
    fun alReanudar() {
        val deviceOnline = connectivityObserver.isCurrentlyOnline()
        _uiState.update { it.copy(isOnline = deviceOnline) }
        if (!deviceOnline) return

        viewModelScope.launch {
            sincronizarYRefrescarSiHayRed()
        }
    }

    private suspend fun sincronizarYRefrescarSiHayRed() {
        val currentUser = authRepository.getCurrentUser()
        val filtroCaptador = if (currentUser.role == UserRole.ADMINISTRATIVO) null else currentUser.id

        prospectoRepository.obtenerEventoActivo()?.let { evento ->
            _uiState.update {
                it.copy(
                    activeEventId = evento.id,
                    activeEventName = evento.nombre
                )
            }
        }

        autoSincronizarPendientes()

        val serverOk = prospectoRepository.refrescarDesdeServidor(filtroCaptador)
        _uiState.update { it.copy(isOnline = connectivityObserver.isCurrentlyOnline() && serverOk) }
    }

    private suspend fun autoSincronizarPendientes() {
        if (_uiState.value.isSyncing) return
        val pendingIds = latestProspectos
            .filter { it.syncStatus == SyncStatus.PENDING_SYNC }
            .map { it.id }
            .toSet()

        if (pendingIds.isEmpty() || pendingIds == lastAutoSyncPendingIds) return

        lastAutoSyncPendingIds = pendingIds
        val currentUser = authRepository.getCurrentUser()
        _uiState.update { it.copy(isSyncing = true) }

        val result = prospectoRepository.sincronizarNuevosPendientes(
            captadorId = currentUser.id,
            eventoId = _uiState.value.activeEventId
        )

        if (result.exitosos > 0 && result.fallidos == 0) {
            lastAutoSyncPendingIds = emptySet()
        }

        _uiState.update {
            it.copy(
                isSyncing = false,
                isOnline = !result.huboErrorRed,
                syncMessage = result.mensaje
            )
        }
    }

    fun sincronizarAhora() {
        if (_uiState.value.isSyncing) return
        val currentUser = authRepository.getCurrentUser()
        lastAutoSyncPendingIds = emptySet()

        _uiState.update { it.copy(isSyncing = true, syncMessage = null) }
        viewModelScope.launch {
            val result = prospectoRepository.sincronizarPendientes(
                captadorId = currentUser.id,
                eventoId = _uiState.value.activeEventId
            )
            _uiState.update {
                it.copy(
                    isSyncing = false,
                    isOnline = !result.huboErrorRed,
                    syncMessage = result.mensaje
                )
            }
        }
    }

    fun poblarDatosDemo() {
        viewModelScope.launch {
            val count = prospectoRepository.poblarDatosDemo(resetear = true)
            lastAutoSyncPendingIds = emptySet()
            _uiState.update {
                it.copy(syncMessage = "Datos demo restablecidos ($count prospectos).")
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }

    private fun isSameLocalDay(timestampEpoch: Long): Boolean {
        val target = Calendar.getInstance().apply { timeInMillis = timestampEpoch }
        val now = Calendar.getInstance()
        return target.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            target.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
    }
}
