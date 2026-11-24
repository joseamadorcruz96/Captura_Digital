package com.therionlabs.captura_digital_v2.ui.capturas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.therionlabs.captura_digital_v2.data.model.FormularioBorrador
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.UserRole
import com.therionlabs.captura_digital_v2.data.repository.AuthRepository
import com.therionlabs.captura_digital_v2.data.repository.BorradorRepository
import com.therionlabs.captura_digital_v2.data.repository.IBorradorRepository
import com.therionlabs.captura_digital_v2.data.repository.IProspectoRepository
import com.therionlabs.captura_digital_v2.data.repository.ProspectoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la vista consolidada "Mis Capturas y Borradores".
 * Separa estrictamente los borradores locales (no sincronizables) de las capturas válidas
 * listas o ya sincronizadas (Completas y Express).
 */
class CapturasViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val prospectoRepository: IProspectoRepository = ProspectoRepository(),
    private val borradorRepository: IBorradorRepository = BorradorRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CapturasUiState(
            appMode = authRepository.appMode,
            isAdmin = authRepository.getCurrentUser().role == UserRole.ADMINISTRATIVO
        )
    )
    val uiState: StateFlow<CapturasUiState> = _uiState.asStateFlow()

    private var borradoresObserverJob: Job? = null
    private var allCapturas: List<Prospecto> = emptyList()

    init {
        viewModelScope.launch {
            authRepository.sessionState.collect { session ->
                val user = session.user ?: authRepository.getCurrentUser()
                val isAdmin = user.role == UserRole.ADMINISTRATIVO
                _uiState.update {
                    it.copy(
                        appMode = session.appMode,
                        isAdmin = isAdmin,
                        capturas = filtrarCapturasPorRol(allCapturas, isAdmin, user.id)
                    )
                }
                observarBorradores(user.id, isAdmin)
            }
        }

        viewModelScope.launch {
            prospectoRepository.observeProspectos().collect { prospectos ->
                allCapturas = prospectos
                val user = authRepository.getCurrentUser()
                val isAdmin = user.role == UserRole.ADMINISTRATIVO
                _uiState.update {
                    it.copy(
                        capturas = filtrarCapturasPorRol(prospectos, isAdmin, user.id)
                    )
                }
            }
        }
    }

    private fun observarBorradores(userId: Int, isAdmin: Boolean) {
        borradoresObserverJob?.cancel()
        borradoresObserverJob = viewModelScope.launch {
            val flow = if (isAdmin) {
                borradorRepository.observeAll()
            } else {
                borradorRepository.observeByCaptador(userId)
            }
            flow.collect { drafts ->
                _uiState.update { it.copy(borradores = drafts) }
            }
        }
    }

    private fun filtrarCapturasPorRol(
        prospectos: List<Prospecto>,
        isAdmin: Boolean,
        userId: Int
    ): List<Prospecto> {
        val list = if (isAdmin) prospectos else prospectos.filter { it.captadorId == userId }
        return list.sortedByDescending { it.fechaCaptura }
    }

    fun onFilterSelected(filter: CapturasFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun abrirDetalle(prospecto: Prospecto) {
        _uiState.update { it.copy(selectedProspectoDetail = prospecto) }
    }

    fun cerrarDetalle() {
        _uiState.update { it.copy(selectedProspectoDetail = null) }
    }

    fun descartarBorrador(borrador: FormularioBorrador) {
        viewModelScope.launch {
            borradorRepository.eliminarBorrador(borrador.id)
            _uiState.update {
                it.copy(statusMessage = "Borrador '${borrador.tituloResumen}' descartado.")
            }
        }
    }

    fun sincronizarPendientes() {
        if (_uiState.value.isSyncing) return
        val user = authRepository.getCurrentUser()
        _uiState.update { it.copy(isSyncing = true, statusMessage = null) }
        viewModelScope.launch {
            val result = prospectoRepository.sincronizarPendientes(captadorId = user.id)
            _uiState.update {
                it.copy(
                    isSyncing = false,
                    statusMessage = result.mensaje
                )
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
