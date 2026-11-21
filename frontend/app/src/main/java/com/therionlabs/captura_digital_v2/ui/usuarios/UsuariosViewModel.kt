package com.therionlabs.captura_digital_v2.ui.usuarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.therionlabs.captura_digital_v2.data.model.User
import com.therionlabs.captura_digital_v2.data.model.UserRole
import com.therionlabs.captura_digital_v2.data.repository.UsuarioRepository
import com.therionlabs.captura_digital_v2.util.AuthValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class UsuariosViewModel(
    private val repository: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsuariosUiState())
    val uiState: StateFlow<UsuariosUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun onNombreChange(value: String) {
        _uiState.value = _uiState.value.copy(nombre = value, nombreError = null, mensaje = null)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, emailError = null, error = null, mensaje = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, passwordError = null, mensaje = null)
    }

    fun onRolChange(value: UserRole) {
        _uiState.value = _uiState.value.copy(rol = value)
    }

    fun editar(usuario: User) {
        _uiState.value = _uiState.value.copy(
            editandoId = usuario.id,
            nombre = usuario.name,
            email = usuario.email,
            password = "",
            rol = usuario.role,
            nombreError = null,
            emailError = null,
            passwordError = null,
            error = null,
            mensaje = null
        )
    }

    fun cancelarEdicion() {
        _uiState.value = UsuariosUiState(usuarios = _uiState.value.usuarios)
    }

    fun guardar() {
        val state = _uiState.value
        val email = state.email.trim()

        val nombreError = AuthValidator.validarNombre(state.nombre)
        val emailError = AuthValidator.validarEmail(email)
        val passwordError = AuthValidator.validarPassword(state.password)

        if (nombreError != null || emailError != null || passwordError != null) {
            _uiState.value = state.copy(
                nombreError = nombreError,
                emailError = emailError,
                passwordError = passwordError
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            try {
                val id = state.editandoId
                val error = if (id == null) {
                    repository.crear(state.nombre.trim(), email, state.password, state.rol)
                } else {
                    repository.actualizar(id, state.nombre.trim(), email, state.password, state.rol)
                }

                if (error != null) {
                    _uiState.value = _uiState.value.copy(cargando = false, error = error)
                } else {
                    val mensaje = if (id == null) "Usuario creado correctamente" else "Usuario actualizado correctamente"
                    _uiState.value = UsuariosUiState(mensaje = mensaje)
                    cargar()
                }
            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(cargando = false, error = "No se pudo conectar con el servidor")
            }
        }
    }

    fun eliminar(id: Int) {
        viewModelScope.launch {
            try {
                val error = repository.eliminar(id)
                if (error != null) {
                    _uiState.value = _uiState.value.copy(error = error)
                } else {
                    _uiState.value = _uiState.value.copy(mensaje = "Usuario eliminado correctamente")
                    cargar()
                }
            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(error = "No se pudo conectar con el servidor")
            }
        }
    }

    private fun cargar() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(usuarios = repository.listar(), cargando = false)
            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(cargando = false, error = "No se pudo conectar con el servidor")
            }
        }
    }
}