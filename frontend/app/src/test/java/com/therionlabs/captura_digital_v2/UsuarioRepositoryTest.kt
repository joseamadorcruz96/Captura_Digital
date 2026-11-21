package com.therionlabs.captura_digital_v2

import com.therionlabs.captura_digital_v2.data.model.UserRole
import com.therionlabs.captura_digital_v2.data.repository.UsuarioRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UsuarioRepositoryTest {

    private val api = FakeAuthApi()
    private val repository = UsuarioRepository(api)

    @Test
    fun listar_devuelveUsuariosConSuRol() = runTest {
        val usuarios = repository.listar()

        assertEquals(2, usuarios.size)
        assertEquals(UserRole.ADMINISTRATIVO, usuarios[0].role)
    }

    @Test
    fun crear_conCorreoNuevo_noDevuelveError() = runTest {
        assertNull(repository.crear("Nuevo", "nuevo@captura.cl", "123456", UserRole.CAPTADOR))
        assertEquals(3, api.usuarios.size)
    }

    @Test
    fun crear_conCorreoRepetido_devuelveError() = runTest {
        assertEquals(
            "El correo ya está registrado",
            repository.crear("Otro", "admin@captura.cl", "123456", UserRole.CAPTADOR)
        )
    }

    @Test
    fun crear_conErrorDelServidor_devuelveErrorGeneral() = runTest {
        api.errorServidor = true

        assertEquals(
            "No se pudo crear el usuario",
            repository.crear("Nuevo", "nuevo@captura.cl", "123456", UserRole.CAPTADOR)
        )
    }

    @Test
    fun actualizar_cambiaLosDatos() = runTest {
        assertNull(repository.actualizar(2, "Captador Editado", "captador@captura.cl", "123456", UserRole.CAPTADOR))
        assertEquals("Captador Editado", api.usuarios.first { it.id == 2 }.nombre)
    }

    @Test
    fun actualizar_conCorreoDeOtroUsuario_devuelveError() = runTest {
        assertEquals(
            "El correo ya está registrado",
            repository.actualizar(2, "Captador", "admin@captura.cl", "123456", UserRole.CAPTADOR)
        )
    }

    @Test
    fun actualizar_usuarioInexistente_devuelveErrorGeneral() = runTest {
        assertEquals(
            "No se pudo actualizar el usuario",
            repository.actualizar(99, "Nadie", "nadie@captura.cl", "123456", UserRole.CAPTADOR)
        )
    }

    @Test
    fun eliminar_quitaAlUsuario() = runTest {
        assertNull(repository.eliminar(2))
        assertEquals(1, api.usuarios.size)
    }

    @Test
    fun eliminar_usuarioInexistente_devuelveError() = runTest {
        assertEquals("No se pudo eliminar el usuario", repository.eliminar(99))
    }
}
