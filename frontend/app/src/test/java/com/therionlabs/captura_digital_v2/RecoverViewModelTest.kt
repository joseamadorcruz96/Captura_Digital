package com.therionlabs.captura_digital_v2

import com.therionlabs.captura_digital_v2.data.repository.AuthRepository
import com.therionlabs.captura_digital_v2.ui.auth.RecoverViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RecoverViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = FakeAuthApi()
    private val viewModel = RecoverViewModel(AuthRepository(api))

    @Test
    fun correoVacio_muestraError() {
        viewModel.send()

        assertEquals("Ingresa el correo", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.sent)
    }

    @Test
    fun correoInvalido_muestraError() {
        viewModel.onEmailChange("correo-malo")
        viewModel.send()

        assertEquals("Correo inválido", viewModel.uiState.value.error)
    }

    @Test
    fun correoRegistrado_marcaComoEnviado() {
        viewModel.onEmailChange("admin@captura.cl")
        viewModel.send()

        assertTrue(viewModel.uiState.value.sent)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun correoNoRegistrado_muestraError() {
        viewModel.onEmailChange("nadie@captura.cl")
        viewModel.send()

        assertEquals("Este correo no está registrado", viewModel.uiState.value.error)
    }

    @Test
    fun sinConexion_muestraError() {
        api.sinConexion = true
        viewModel.onEmailChange("admin@captura.cl")
        viewModel.send()

        assertEquals("No se pudo conectar con el servidor", viewModel.uiState.value.error)
    }

    @Test
    fun escribir_limpiaElError() {
        viewModel.send()
        viewModel.onEmailChange("a")

        assertNull(viewModel.uiState.value.error)
    }
}
