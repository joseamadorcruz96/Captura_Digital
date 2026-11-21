package com.therionlabs.captura_digital_v2

import com.therionlabs.captura_digital_v2.ui.auth.LoginViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LoginViewModelTest {

    @Test
    fun loginConCamposVacios_muestraErrorEnCadaCampo() {
        val viewModel = LoginViewModel()

        viewModel.login()

        val state = viewModel.uiState.value
        assertEquals("Ingresa el correo", state.emailError)
        assertEquals("Ingresa la contraseña", state.passwordError)
    }

    @Test
    fun loginConCorreoInvalido_muestraErrorDeCorreo() {
        val viewModel = LoginViewModel()

        viewModel.onEmailChange("correo-malo")
        viewModel.onPasswordChange("123456")
        viewModel.login()

        val state = viewModel.uiState.value
        assertEquals("Correo inválido", state.emailError)
        assertNull(state.passwordError)
    }

    @Test
    fun escribirEnElCampo_limpiaElError() {
        val viewModel = LoginViewModel()

        viewModel.login()
        viewModel.onEmailChange("admin@captura.cl")

        assertNull(viewModel.uiState.value.emailError)
    }
}