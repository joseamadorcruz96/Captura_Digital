package com.therionlabs.captura_digital_v2

import com.therionlabs.captura_digital_v2.data.local.InMemorySessionStorage
import com.therionlabs.captura_digital_v2.data.local.LocalUserDataSource
import com.therionlabs.captura_digital_v2.data.model.AppMode
import com.therionlabs.captura_digital_v2.data.model.UserRole
import com.therionlabs.captura_digital_v2.data.repository.AuthRepository
import com.therionlabs.captura_digital_v2.ui.auth.LoginViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = FakeAuthApi()
    private val viewModel = LoginViewModel(AuthRepository(api))

    @After
    fun tearDown() {
        AuthRepository(api).logout()
    }

    @Test
    fun loginConCamposVacios_muestraErrorEnCadaCampo() {
        viewModel.login()

        val state = viewModel.uiState.value
        assertEquals("Ingresa el correo", state.emailError)
        assertEquals("Ingresa la contraseña", state.passwordError)
    }

    @Test
    fun loginConCorreoInvalido_muestraErrorDeCorreo() {
        viewModel.onEmailChange("correo-malo")
        viewModel.onPasswordChange("123456")
        viewModel.login()

        val state = viewModel.uiState.value
        assertEquals("Correo inválido", state.emailError)
        assertNull(state.passwordError)
    }

    @Test
    fun escribirEnElCampo_limpiaElError() {
        viewModel.login()
        viewModel.onEmailChange("admin@captura.cl")
        viewModel.onPasswordChange("123456")

        assertNull(viewModel.uiState.value.emailError)
        assertNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun loginCorrecto_guardaElUsuario() {
        viewModel.onEmailChange("admin@captura.cl")
        viewModel.onPasswordChange("123456")
        viewModel.login()

        val state = viewModel.uiState.value
        assertNotNull(state.user)
        assertNull(state.error)
    }

    @Test
    fun loginConContrasenaIncorrecta_muestraError() {
        viewModel.onEmailChange("admin@captura.cl")
        viewModel.onPasswordChange("000000")
        viewModel.login()

        assertEquals("Correo o contraseña incorrectos", viewModel.uiState.value.error)
    }

    @Test
    fun loginSinConexion_muestraError() {
        api.sinConexion = true
        viewModel.onEmailChange("admin@captura.cl")
        viewModel.onPasswordChange("123456")
        viewModel.login()

        assertEquals("No se pudo conectar con el servidor", viewModel.uiState.value.error)
    }

    @Test
    fun localDemo_cambiarModoYSeleccionarCuentaDemoPermiteLoginOffline() {
        api.sinConexion = true
        val storage = InMemorySessionStorage(defaultAppMode = AppMode.REMOTE_GATEWAY)
        val localRepo = AuthRepository(
            api = api,
            localDataSource = LocalUserDataSource(),
            sessionStorage = storage
        )
        val vm = LoginViewModel(localRepo)

        vm.onAppModeChange(AppMode.LOCAL_DEMO)
        assertEquals(AppMode.LOCAL_DEMO, vm.uiState.value.appMode)
        assertEquals(AppMode.LOCAL_DEMO, storage.currentAppMode)

        vm.seleccionarCuentaDemo(LocalUserDataSource.DEMO_PROSPECTO.email)
        vm.login()

        val state = vm.uiState.value
        assertEquals(UserRole.PROSPECTO, state.user?.role)
        assertNull(state.error)
    }
}
