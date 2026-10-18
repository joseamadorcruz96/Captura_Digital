package com.therionlabs.captura_digital_v2.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.therionlabs.captura_digital_v2.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

        val emailError = when {
            email.isBlank() -> "Ingresa tu correo"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Correo inválido"
            else -> null
        }

        val passwordError = when {
            state.password.isBlank() -> "Ingresa tu contraseña"
            state.password.length < 6 -> "Debe tener al menos 6 caracteres"
            else -> null
        }

        if (emailError != null || passwordError != null) {
            _uiState.value = state.copy(emailError = emailError, passwordError = passwordError)
            return
        }

        val user = repository.login(email, state.password)

        _uiState.value = if (user == null) {
            state.copy(error = "Correo o contraseña incorrectos")
        } else {
            state.copy(user = user)
        }
    }
}