package com.therionlabs.captura_digital_v2.ui.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.therionlabs.captura_digital_v2.data.model.SyncStatus
import com.therionlabs.captura_digital_v2.data.model.UserRole
import com.therionlabs.captura_digital_v2.data.repository.AuthRepository
import com.therionlabs.captura_digital_v2.data.repository.IProspectoRepository
import com.therionlabs.captura_digital_v2.data.repository.ProspectoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InicioViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val prospectoRepository: IProspectoRepository = ProspectoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(InicioUiState(user = authRepository.getCurrentUser()))
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prospectoRepository.observeProspectos().collect { prospectos ->
                val currentUser = authRepository.getCurrentUser()
                val capturedCount = if (currentUser.role == UserRole.ADMINISTRATIVO) {
                    prospectos.size
                } else {
                    prospectos.count { it.captadorId == currentUser.id }
                }
                val pending = prospectos.count { it.syncStatus != SyncStatus.SYNCED }

                _uiState.value = _uiState.value.copy(
                    user = currentUser,
                    totalCapturedToday = capturedCount,
                    pendingSyncCount = pending
                )
            }
        }
    }

    fun sincronizarAhora() {
        if (_uiState.value.isSyncing) return
        val currentUser = authRepository.getCurrentUser()

        _uiState.value = _uiState.value.copy(isSyncing = true, syncMessage = null)
        viewModelScope.launch {
            val result = prospectoRepository.sincronizarPendientes(
                captadorId = currentUser.id,
                eventoId = "EVT-DUOC-2026"
            )
            _uiState.value = _uiState.value.copy(
                isSyncing = false,
                pendingSyncCount = prospectoRepository.getPendingSyncCount(),
                syncMessage = result.mensaje
            )
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
