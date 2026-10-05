package com.therionlabs.captura_digital_v2.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.therionlabs.captura_digital_v2.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RecoverViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(RecoverUiState())
    val uiState: StateFlow<RecoverUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, error = null)
    }

    fun send() {
        val email = _uiState.value.email.trim()

        val error = when {
            email.isBlank() -> "Ingresa tu correo"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Correo inválido"
            !repository.emailExists(email) -> "Este correo no está registrado"
            else -> null
        }

        _uiState.value = if (error != null) {
            _uiState.value.copy(error = error)
        } else {
            _uiState.value.copy(sent = true)
        }
    }
}