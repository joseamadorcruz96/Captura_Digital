package com.therionlabs.captura_digital_v2.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.therionlabs.captura_digital_v2.data.local.LocalUserDataSource
import com.therionlabs.captura_digital_v2.data.model.AppMode
import com.therionlabs.captura_digital_v2.data.repository.AuthRepository
import com.therionlabs.captura_digital_v2.util.AuthValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class LoginViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState(appMode = repository.appMode))
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private var observingMode = false

    fun observarModo() {
        if (observingMode) return
        observingMode = true
        viewModelScope.launch {
            repository.sessionState.collect { session ->
                _uiState.update { it.copy(appMode = session.appMode) }
            }
        }
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, emailError = null, error = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, passwordError = null, error = null)
    }

    fun onAppModeChange(mode: AppMode) {
        viewModelScope.launch {
            repository.setAppMode(mode)
            _uiState.update { it.copy(appMode = mode, error = null) }
        }
    }

    fun seleccionarCuentaDemo(
        email: String,
        password: String = LocalUserDataSource.DEMO_PASSWORD
    ) {
        _uiState.update {
            it.copy(
                email = email,
                password = password,
                emailError = null,
                passwordError = null,
                error = null
            )
        }
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
            } catch (_: IOException) {
                _uiState.value.copy(error = "No se pudo conectar con el servidor")
            }
        }
    }
}
