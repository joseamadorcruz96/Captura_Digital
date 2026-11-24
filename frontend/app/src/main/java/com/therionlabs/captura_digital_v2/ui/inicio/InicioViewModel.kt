package com.therionlabs.captura_digital_v2.ui.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.therionlabs.captura_digital_v2.data.local.DemoSeedGenerator
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.ResumenCierreTurno
import com.therionlabs.captura_digital_v2.data.model.SyncStatus
import com.therionlabs.captura_digital_v2.data.model.TurnoEvento
import com.therionlabs.captura_digital_v2.data.model.UserRole
import com.therionlabs.captura_digital_v2.data.remote.ConnectivityObserver
import com.therionlabs.captura_digital_v2.data.remote.NetworkMonitor
import com.therionlabs.captura_digital_v2.data.repository.AuthRepository
import com.therionlabs.captura_digital_v2.data.repository.BorradorRepository
import com.therionlabs.captura_digital_v2.data.repository.EventoRepository
import com.therionlabs.captura_digital_v2.data.repository.IBorradorRepository
import com.therionlabs.captura_digital_v2.data.repository.IEventoRepository
import com.therionlabs.captura_digital_v2.data.repository.IProspectoRepository
import com.therionlabs.captura_digital_v2.data.repository.ProspectoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class InicioViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val prospectoRepository: IProspectoRepository = ProspectoRepository(),
    private val connectivityObserver: ConnectivityObserver = NetworkMonitor.observer(),
    private val eventoRepository: IEventoRepository = EventoRepository(),
    private val nowProvider: () -> Long = { System.currentTimeMillis() },
    private val borradorRepository: IBorradorRepository = BorradorRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        InicioUiState(
            user = authRepository.getCurrentUser(),
            appMode = authRepository.appMode,
            activeEventId = DemoSeedGenerator.DEFAULT_EVENT_ID,
            activeEventName = DemoSeedGenerator.DEFAULT_EVENT_NAME,
            isOnline = connectivityObserver.isCurrentlyOnline()
        )
    )
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    private var latestProspectos: List<Prospecto> = emptyList()
    private var lastAutoSyncPendingIds: Set<String> = emptySet()
    private var turnoObserverJob: Job? = null
    private var borradoresObserverJob: Job? = null
    private var observedTurnoUserId: Int? = null

    init {
        viewModelScope.launch {
            authRepository.sessionState.collect { session ->
                val resolvedUser = session.user ?: authRepository.getCurrentUser()
                _uiState.update { state ->
                    state.copy(
                        user = resolvedUser,
                        appMode = session.appMode
                    )
                }
                if (observedTurnoUserId != resolvedUser.id) {
                    observedTurnoUserId = resolvedUser.id
                    observarTurnoUsuario(resolvedUser.id)
                    observarBorradoresUsuario(resolvedUser.id, resolvedUser.role)
                }
                recalcularMetricas()
            }
        }

        viewModelScope.launch {
            eventoRepository.observeEventosDisponibles().collect { disponibles ->
                _uiState.update { it.copy(availableEvents = disponibles) }
            }
        }

        viewModelScope.launch {
            eventoRepository.observeEventoActivo().collect { evento ->
                if (evento != null) {
                    _uiState.update {
                        it.copy(
                            activeEventId = evento.id,
                            activeEventName = evento.nombre,
                            activeEvent = evento
                        )
                    }
                    recalcularMetricas()
                }
            }
        }

        viewModelScope.launch {
            prospectoRepository.observeProspectos().collect { prospectos ->
                latestProspectos = prospectos
                recalcularMetricas()
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

    private fun observarTurnoUsuario(captadorId: Int) {
        turnoObserverJob?.cancel()
        turnoObserverJob = viewModelScope.launch {
            eventoRepository.observeTurnoActivo(captadorId).collect { turno ->
                _uiState.update { it.copy(activeTurno = turno) }
                recalcularMetricas()
            }
        }
    }

    private fun observarBorradoresUsuario(captadorId: Int, role: UserRole) {
        borradoresObserverJob?.cancel()
        borradoresObserverJob = viewModelScope.launch {
            val flow = if (role == UserRole.ADMINISTRATIVO) {
                borradorRepository.observeAll()
            } else {
                borradorRepository.observeByCaptador(captadorId)
            }
            flow.collect { drafts ->
                _uiState.update { it.copy(borradoresCount = drafts.size) }
            }
        }
    }

    private fun recalcularMetricas() {
        val currentUser = authRepository.getCurrentUser()
        val stateSnapshot = _uiState.value
        val activeEventId = stateSnapshot.activeEventId
        val activeTurno = stateSnapshot.activeTurno
        val prospectos = latestProspectos

        val capturedToday = if (currentUser.role == UserRole.ADMINISTRATIVO) {
            prospectos.size
        } else {
            prospectos.count { it.captadorId == currentUser.id && isSameLocalDay(it.fechaCaptura) }
        }

        val eventCaptured = prospectos.count { it.eventoId == activeEventId }
        val turnoCaptured = if (activeTurno != null) {
            contarCapturasDeTurno(prospectos, activeTurno, currentUser.role, currentUser.id)
        } else {
            0
        }

        val pending = prospectos.count { it.syncStatus != SyncStatus.SYNCED }
        val syncErrors = prospectos.count { it.syncStatus == SyncStatus.SYNC_ERROR }
        val totalCapturas = if (currentUser.role == UserRole.ADMINISTRATIVO) {
            prospectos.size
        } else {
            prospectos.count { it.captadorId == currentUser.id }
        }

        _uiState.update { state ->
            // Si entraron nuevos pendientes, limpiamos mensajes de éxito anteriores para no contradecir el contador
            val clearStaleMessage = pending > 0 &&
                !state.isSyncing &&
                (state.syncMessage?.startsWith("Sincronización completada") == true ||
                    state.syncMessage?.startsWith("Todo al día") == true)
            state.copy(
                user = currentUser,
                totalCapturedToday = capturedToday,
                totalCapturasCount = totalCapturas,
                eventCapturedCount = eventCaptured,
                turnoCapturedCount = turnoCaptured,
                pendingSyncCount = pending,
                syncErrorCount = syncErrors,
                syncMessage = if (clearStaleMessage) null else state.syncMessage
            )
        }
    }

    private fun contarCapturasDeTurno(
        prospectos: List<Prospecto>,
        turno: TurnoEvento,
        role: UserRole,
        userId: Int
    ): Int = filtrarCapturasDeTurno(prospectos, turno, role, userId).size

    private fun filtrarCapturasDeTurno(
        prospectos: List<Prospecto>,
        turno: TurnoEvento,
        role: UserRole,
        userId: Int
    ): List<Prospecto> {
        return prospectos.filter { item ->
            val perteneceEvento = item.eventoId == turno.eventoId
            val perteneceUsuario = role == UserRole.ADMINISTRATIVO || item.captadorId == userId
            item.fechaCaptura >= turno.inicioEpoch && perteneceEvento && perteneceUsuario
        }
    }

    /**
     * Refresca eventos, evento activo y datos remotos al entrar o volver a Inicio,
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

        eventoRepository.refrescarEventosDesdeServidor()
        eventoRepository.getEventoActivo()?.let { evento ->
            _uiState.update {
                it.copy(
                    activeEventId = evento.id,
                    activeEventName = evento.nombre,
                    activeEvent = evento
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

    fun seleccionarEvento(eventoId: String) {
        viewModelScope.launch {
            val seleccionado = eventoRepository.seleccionarEventoActivo(eventoId) ?: return@launch
            _uiState.update {
                it.copy(
                    activeEventId = seleccionado.id,
                    activeEventName = seleccionado.nombre,
                    activeEvent = seleccionado
                )
            }
            recalcularMetricas()
        }
    }

    fun iniciarTurno(metaPersonalizada: Int? = null) {
        val currentUser = authRepository.getCurrentUser()
        val state = _uiState.value
        val meta = (metaPersonalizada ?: state.activeEvent?.metaProspectos ?: 20).coerceAtLeast(1)

        viewModelScope.launch {
            val turno = eventoRepository.iniciarTurno(
                captadorId = currentUser.id,
                eventoId = state.activeEventId,
                metaTurno = meta
            )
            _uiState.update { it.copy(activeTurno = turno, resumenCierreTurno = null) }
            recalcularMetricas()
        }
    }

    fun finalizarTurno() {
        val turno = _uiState.value.activeTurno ?: return
        val currentUser = authRepository.getCurrentUser()
        val capturas = filtrarCapturasDeTurno(
            prospectos = latestProspectos,
            turno = turno,
            role = currentUser.role,
            userId = currentUser.id
        )
        val distribucion = NivelInteres.entries.associateWith { nivel ->
            capturas.count { it.nivelInteres == nivel }
        }
        val pendientes = latestProspectos.count { it.syncStatus != SyncStatus.SYNCED }
        val eventoNombre = _uiState.value.availableEvents
            .firstOrNull { it.id == turno.eventoId }
            ?.nombre
            ?: _uiState.value.activeEventName

        viewModelScope.launch {
            val cerrado = eventoRepository.finalizarTurno(turno.id, capturas.size)
            val finEpoch = cerrado?.finEpoch ?: nowProvider()
            val resumen = ResumenCierreTurno(
                turnoId = turno.id,
                eventoId = turno.eventoId,
                eventoNombre = eventoNombre,
                inicioEpoch = turno.inicioEpoch,
                finEpoch = finEpoch,
                duracionMs = (finEpoch - turno.inicioEpoch).coerceAtLeast(0L),
                metaTurno = turno.metaTurno,
                totalCapturados = capturas.size,
                distribucionInteres = distribucion,
                pendientesSync = pendientes
            )
            _uiState.update {
                it.copy(
                    activeTurno = null,
                    turnoCapturedCount = 0,
                    resumenCierreTurno = resumen
                )
            }
        }
    }

    fun cerrarResumenTurno() {
        _uiState.update { it.copy(resumenCierreTurno = null) }
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
            eventoRepository.poblarEventosDemo(resetear = false)
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
