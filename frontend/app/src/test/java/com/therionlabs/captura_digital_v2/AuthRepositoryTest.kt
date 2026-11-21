package com.therionlabs.captura_digital_v2

import com.therionlabs.captura_digital_v2.data.model.UserRole
import com.therionlabs.captura_digital_v2.data.repository.AuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryTest {

    private val api = FakeAuthApi()
    private val repository = AuthRepository(api)

    @After
    fun tearDown() {
        repository.logout()
    }

    @Test
    fun loginCorrecto_devuelveUsuarioYQuedaEnSesion() = runTest {
        val user = repository.login("admin@captura.cl", "123456")

        assertEquals("Administrador", user?.name)
        assertEquals(UserRole.ADMINISTRATIVO, user?.role)
        assertEquals(1, repository.getCurrentUser().id)
    }

    @Test
    fun loginIncorrecto_devuelveNull() = runTest {
        assertNull(repository.login("admin@captura.cl", "000000"))
    }

    @Test
    fun sinSesion_devuelveCaptadorPorDefecto() {
        assertEquals(UserRole.CAPTADOR, repository.getCurrentUser().role)
    }

    @Test
    fun logout_cierraLaSesion() = runTest {
        repository.login("admin@captura.cl", "123456")
        repository.logout()

        assertEquals(UserRole.CAPTADOR, repository.getCurrentUser().role)
    }

    @Test
    fun recuperarConCorreoRegistrado_devuelveTrue() = runTest {
        assertTrue(repository.recuperar("admin@captura.cl"))
    }

    @Test
    fun recuperarConCorreoNoRegistrado_devuelveFalse() = runTest {
        assertFalse(repository.recuperar("nadie@captura.cl"))
    }
}
