package com.therionlabs.captura_digital_v2.ui.inicio

import androidx.lifecycle.ViewModel
import com.therionlabs.captura_digital_v2.data.repository.AuthRepository
import com.therionlabs.captura_digital_v2.data.repository.SurveyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InicioViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val surveyRepository = SurveyRepository()

    private val _uiState = MutableStateFlow(InicioUiState())
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val currentUser = authRepository.getCurrentUser()
        val captures = surveyRepository.getByCaptador(currentUser.id)

        _uiState.value = _uiState.value.copy(
            user = currentUser,
            totalCapturedToday = captures.size,
            pendingSyncCount = 0 // En memoria por ahora
        )
    }

    fun logout() {
        authRepository.logout()
    }
}
