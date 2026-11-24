package com.therionlabs.captura_digital_v2

import com.therionlabs.captura_digital_v2.data.model.FormularioBorrador
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
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

    @Test
    fun validarSedeNivelYPerfil_rechazanNull() {
        assertTrue(FormularioValidator.validarSede(null) is Resultado.Invalido)
        assertTrue(FormularioValidator.validarSede("") is Resultado.Invalido)
        assertTrue(FormularioValidator.validarSede("Viña del Mar") is Resultado.Validado)

        assertTrue(FormularioValidator.validarNivelInteres(null) is Resultado.Invalido)
        assertTrue(FormularioValidator.validarNivelInteres(NivelInteres.ALTO) is Resultado.Validado)

        assertTrue(FormularioValidator.validarPerfilMotivacional(null) is Resultado.Invalido)
        assertTrue(FormularioValidator.validarPerfilMotivacional(PerfilMotivacional.VOCACIONAL) is Resultado.Validado)
    }

    @Test
    fun validarConsentimientoConFirma_exigeFirmaSoloCuandoFlagEstaActivo() {
        assertTrue(FormularioValidator.validarConsentimientoConFirma(true, null, exigirFirma = false).isValid)
        assertFalse(FormularioValidator.validarConsentimientoConFirma(true, null, exigirFirma = true).isValid)
        assertTrue(FormularioValidator.validarConsentimientoConFirma(true, "base64-png", exigirFirma = true).isValid)
    }

    @Test
    fun validarBorradorMinimo_rechazaVacioYAceptaUnCampo() {
        val vacio = FormularioBorrador(captadorId = 2, consentimiento = true)
        assertFalse(FormularioValidator.validarBorradorMinimo(vacio).isValid)

        val conNombre = FormularioBorrador(captadorId = 2, nombres = "Camila")
        assertTrue(FormularioValidator.validarBorradorMinimo(conNombre).isValid)
    }

    @Test
    fun validarExpress_validaMinimosEsencialesSinExigirColegioNiComuna() {
        val ok = FormularioValidator.validarExpress(
            consentimiento = true,
            firmaBase64 = null,
            exigirFirma = false,
            rut = "11.111.111-1",
            nombres = "Camila",
            apellidos = "Soto",
            email = "camila@duoc.cl",
            celular = "",
            sedeInteres = "Viña del Mar",
            carreraPrioridad1 = "Ingeniería en Informática"
        )
        assertTrue(ok.isEmpty())

        val faltantes = FormularioValidator.validarExpress(
            consentimiento = true,
            firmaBase64 = null,
            exigirFirma = false,
            rut = "11.111.111-1",
            nombres = "Camila",
            apellidos = "Soto",
            email = "",
            celular = "",
            sedeInteres = null,
            carreraPrioridad1 = ""
        )
        assertTrue(faltantes.size >= 3)
    }
}
