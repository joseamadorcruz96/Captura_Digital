package com.therionlabs.captura_digital_v2

import com.therionlabs.captura_digital_v2.util.AuthValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun emailVacio_devuelveError() {
        assertEquals("Ingresa el correo", AuthValidator.validarEmail(""))
    }

    @Test
    fun emailSinFormato_devuelveError() {
        assertEquals("Correo inválido", AuthValidator.validarEmail("admin.captura.cl"))
    }

    @Test
    fun emailValido_noDevuelveError() {
        assertNull(AuthValidator.validarEmail("admin@captura.cl"))
    }

    @Test
    fun passwordVacia_devuelveError() {
        assertEquals("Ingresa la contraseña", AuthValidator.validarPassword(""))
    }

    @Test
    fun passwordCorta_devuelveError() {
        assertEquals("Debe tener al menos 6 caracteres", AuthValidator.validarPassword("123"))
    }

    @Test
    fun passwordValida_noDevuelveError() {
        assertNull(AuthValidator.validarPassword("123456"))
    }

    @Test
    fun nombreVacio_devuelveError() {
        assertEquals("Ingresa el nombre", AuthValidator.validarNombre("   "))
    }

    @Test
    fun nombreValido_noDevuelveError() {
        assertNull(AuthValidator.validarNombre("Gonzalo"))
    }
}