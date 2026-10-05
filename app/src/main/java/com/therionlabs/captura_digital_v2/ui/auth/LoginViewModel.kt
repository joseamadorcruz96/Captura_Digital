package com.therionlabs.captura_digital_v2.ui.auth

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
        _uiState.value = _uiState.value.copy(email = value, error = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun login() {
        val state = _uiState.value

        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Complete todos los campos")
            return
        }

        val user = repository.login(state.email, state.password)

        _uiState.value = if (user == null) {
            state.copy(error = "Correo o contraseña incorrectos")
        } else {
            state.copy(user = user)
        }
    }
}