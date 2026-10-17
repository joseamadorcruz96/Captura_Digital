package com.therionlabs.captura_digital_v2.ui.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.therionlabs.captura_digital_v2.data.repository.AuthRepository
import com.therionlabs.captura_digital_v2.data.repository.ProspectoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InicioViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val prospectoRepository = ProspectoRepository()

    private val _uiState = MutableStateFlow(InicioUiState())
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prospectoRepository.prospectosFlow.collect { prospectos ->
                val currentUser = authRepository.getCurrentUser()
                val captures = prospectos.filter { it.captadorId == currentUser.id }
                val pending = prospectos.count { it.syncStatus.name.startsWith("PENDING") }

                _uiState.value = _uiState.value.copy(
                    user = currentUser,
                    totalCapturedToday = captures.size,
                    pendingSyncCount = pending
                )
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
