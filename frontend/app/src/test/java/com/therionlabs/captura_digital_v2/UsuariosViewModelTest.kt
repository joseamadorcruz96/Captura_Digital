package com.therionlabs.captura_digital_v2

import com.therionlabs.captura_digital_v2.data.model.UserRole
import com.therionlabs.captura_digital_v2.data.repository.UsuarioRepository
import com.therionlabs.captura_digital_v2.ui.usuarios.UsuariosViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class UsuariosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = FakeAuthApi()

    private fun crearViewModel() = UsuariosViewModel(UsuarioRepository(api))

    private fun llenarFormulario(viewModel: UsuariosViewModel, email: String) {
        viewModel.onNombreChange("Nuevo Captador")
        viewModel.onEmailChange(email)
        viewModel.onPasswordChange("123456")
        viewModel.onRolChange(UserRole.CAPTADOR)
    }

    @Test
    fun alIniciar_cargaLosUsuarios() {
        val viewModel = crearViewModel()

        assertEquals(2, viewModel.uiState.value.usuarios.size)
    }

    @Test
    fun alIniciarSinConexion_muestraError() {
        api.sinConexion = true
        val viewModel = crearViewModel()

        assertEquals("No se pudo conectar con el servidor", viewModel.uiState.value.error)
    }

    @Test
    fun guardarConCamposVacios_muestraErrores() {
        val viewModel = crearViewModel()

        viewModel.guardar()

        val state = viewModel.uiState.value
        assertEquals("Ingresa el nombre", state.nombreError)
        assertEquals("Ingresa el correo", state.emailError)
        assertEquals("Ingresa la contraseña", state.passwordError)
    }

    @Test
    fun crearUsuario_muestraMensajeYRecargaLista() {
        val viewModel = crearViewModel()
        llenarFormulario(viewModel, "nuevo@captura.cl")

        viewModel.guardar()

        val state = viewModel.uiState.value
        assertEquals("Usuario creado correctamente", state.mensaje)
        assertEquals(3, state.usuarios.size)
        assertEquals("", state.nombre)
    }

    @Test
    fun crearUsuarioConCorreoRepetido_muestraError() {
        val viewModel = crearViewModel()
        llenarFormulario(viewModel, "admin@captura.cl")

        viewModel.guardar()

        assertEquals("El correo ya está registrado", viewModel.uiState.value.error)
    }

    @Test
    fun guardarSinConexion_muestraError() {
        val viewModel = crearViewModel()
        llenarFormulario(viewModel, "nuevo@captura.cl")
        api.sinConexion = true

        viewModel.guardar()

        assertEquals("No se pudo conectar con el servidor", viewModel.uiState.value.error)
    }

    @Test
    fun editar_cargaLosDatosEnElFormulario() {
        val viewModel = crearViewModel()
        val usuario = viewModel.uiState.value.usuarios.first { it.id == 2 }

        viewModel.editar(usuario)

        val state = viewModel.uiState.value
        assertEquals(2, state.editandoId)
        assertEquals("Captador Uno", state.nombre)
        assertEquals("", state.password)
    }

    @Test
    fun guardarEditando_actualizaElUsuario() {
        val viewModel = crearViewModel()
        viewModel.editar(viewModel.uiState.value.usuarios.first { it.id == 2 })
        viewModel.onNombreChange("Captador Editado")
        viewModel.onPasswordChange("654321")

        viewModel.guardar()

        val state = viewModel.uiState.value
        assertEquals("Usuario actualizado correctamente", state.mensaje)
        assertNull(state.editandoId)
        assertEquals("Captador Editado", state.usuarios.first { it.id == 2 }.name)
    }

    @Test
    fun cancelarEdicion_limpiaElFormulario() {
        val viewModel = crearViewModel()
        viewModel.editar(viewModel.uiState.value.usuarios.first { it.id == 2 })

        viewModel.cancelarEdicion()

        val state = viewModel.uiState.value
        assertNull(state.editandoId)
        assertEquals("", state.nombre)
        assertEquals(2, state.usuarios.size)
    }

    @Test
    fun eliminar_quitaAlUsuarioDeLaLista() {
        val viewModel = crearViewModel()

        viewModel.eliminar(2)

        val state = viewModel.uiState.value
        assertEquals("Usuario eliminado correctamente", state.mensaje)
        assertEquals(1, state.usuarios.size)
    }

    @Test
    fun eliminarUsuarioInexistente_muestraError() {
        val viewModel = crearViewModel()

        viewModel.eliminar(99)

        assertEquals("No se pudo eliminar el usuario", viewModel.uiState.value.error)
    }

    @Test
    fun eliminarSinConexion_muestraError() {
        val viewModel = crearViewModel()
        api.sinConexion = true

        viewModel.eliminar(2)

        assertEquals("No se pudo conectar con el servidor", viewModel.uiState.value.error)
    }
}
