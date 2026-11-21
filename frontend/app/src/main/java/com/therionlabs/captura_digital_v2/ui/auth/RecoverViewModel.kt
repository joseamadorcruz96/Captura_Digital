package com.therionlabs.captura_digital_v2.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.therionlabs.captura_digital_v2.data.repository.AuthRepository
import com.therionlabs.captura_digital_v2.util.AuthValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class RecoverViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecoverUiState())
    val uiState: StateFlow<RecoverUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, error = null)
    }

    fun send() {
        val email = _uiState.value.email.trim()

        val error = AuthValidator.validarEmail(email)

        if (error != null) {
            _uiState.value = _uiState.value.copy(error = error)
            return
        }

        viewModelScope.launch {
            _uiState.value = try {
                if (repository.recuperar(email)) {
                    _uiState.value.copy(sent = true)
                } else {
                    _uiState.value.copy(error = "Este correo no está registrado")
                }
            } catch (e: IOException) {
                _uiState.value.copy(error = "No se pudo conectar con el servidor")
            }
        }
    }
}