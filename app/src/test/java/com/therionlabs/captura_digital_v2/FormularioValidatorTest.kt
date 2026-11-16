package com.therionlabs.captura_digital_v2

import com.therionlabs.captura_digital_v2.util.FormularioValidator
import com.therionlabs.captura_digital_v2.util.Resultado
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FormularioValidatorTest {

    @Test
    fun validarConsentimiento_aceptaSoloTrue() {
        assertTrue(FormularioValidator.validarConsentimiento(true) is Resultado.Validado)
        assertTrue(FormularioValidator.validarConsentimiento(false) is Resultado.Invalido)
    }

    @Test
    fun validarNombreYApellido_verificaLongitud() {
        assertTrue(FormularioValidator.validarNombres("") is Resultado.Invalido)
        assertTrue(FormularioValidator.validarNombres("A") is Resultado.Invalido)
        assertTrue(FormularioValidator.validarNombres("Camila") is Resultado.Validado)

        assertTrue(FormularioValidator.validarApellidos(" ") is Resultado.Invalido)
        assertTrue(FormularioValidator.validarApellidos("Soto") is Resultado.Validado)
    }

    @Test
    fun validarRut_usaModulo11() {
        assertTrue(FormularioValidator.validarRut("11.111.111-1") is Resultado.Validado)
        assertTrue(FormularioValidator.validarRut("11.111.111-2") is Resultado.Invalido)
        assertTrue(FormularioValidator.validarRut("") is Resultado.Invalido)
    }

    @Test
    fun validarContacto_requiereAlMenosUnCanalValido() {
        val ambosVacios = FormularioValidator.validarContacto("", "")
        assertFalse(ambosVacios.isValid)

        val soloEmailValido = FormularioValidator.validarContacto("test@duoc.cl", "")
        assertTrue(soloEmailValido.isValid)

        val soloCelularValido = FormularioValidator.validarContacto("", "98765432")
        assertTrue(soloCelularValido.isValid)

        val emailInvalido = FormularioValidator.validarContacto("correo_mal", "")
        assertFalse(emailInvalido.isValid)

        val celularCorto = FormularioValidator.validarContacto("", "1234")
        assertFalse(celularCorto.isValid)
    }

    @Test
    fun validarEstablecimientoYCarrera_noPermitenVacios() {
        assertTrue(FormularioValidator.validarEstablecimiento("") is Resultado.Invalido)
        assertTrue(FormularioValidator.validarEstablecimiento("Liceo A-1") is Resultado.Validado)
        assertTrue(FormularioValidator.validarCarreraPrioridad1("") is Resultado.Invalido)
        assertTrue(FormularioValidator.validarCarreraPrioridad1("Ingeniería en Informática") is Resultado.Validado)
    }
}
