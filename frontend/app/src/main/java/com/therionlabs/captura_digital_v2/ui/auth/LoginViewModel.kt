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

class LoginViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, emailError = null, error = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, passwordError = null, error = null)
    }

    fun login() {
        val state = _uiState.value
        val email = state.email.trim()

        val emailError = AuthValidator.validarEmail(email)
        val passwordError = AuthValidator.validarPassword(state.password)

        if (emailError != null || passwordError != null) {
            _uiState.value = state.copy(emailError = emailError, passwordError = passwordError)
            return
        }

        viewModelScope.launch {
            _uiState.value = try {
                val user = repository.login(email, state.password)
                if (user == null) {
                    _uiState.value.copy(error = "Correo o contraseña incorrectos")
                } else {
                    _uiState.value.copy(user = user)
                }
            } catch (e: IOException) {
                _uiState.value.copy(error = "No se pudo conectar con el servidor")
            }
        }
    }
}