package com.therionlabs.captura_digital_v2

import com.therionlabs.captura_digital_v2.util.CedulaChileParser
import com.therionlabs.captura_digital_v2.util.RutUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CedulaChileParserTest {

    @Test
    fun parse_qrRegistroCivilUrl_extraeRutNormalizado() {
        val payload = "https://portal.sidiv.registrocivil.cl/docstatus?RUN=11111111-1&type=CEDULA&serial=123456789"
        val result = CedulaChileParser.parse(payload)

        assertNotNull(result)
        assertEquals("11.111.111-1", result?.rut)
        assertEquals("QR_REGISTRO_CIVIL", result?.sourceFormat)
    }

    @Test
    fun parse_qrUrlConRunConK_yParametrosOpcionales_extraeRutYNombres() {
        val dv = RutUtils.calculateDv("11111112")
        val payload = "https://portal.sidiv.registrocivil.cl/docstatus?run=11111112-$dv&NOMBRES=CAMILA%20ANDREA&APELLIDOS=VALENZUELA%20SOTO"
        val result = CedulaChileParser.parse(payload)

        assertNotNull(result)
        assertEquals(RutUtils.formatRut("11111112$dv"), result?.rut)
        assertEquals("Camila Andrea", result?.nombres)
        assertEquals("Valenzuela Soto", result?.apellidos)
    }

    @Test
    fun parse_pdf417DelimitadoConPipes_extraeRutNombresApellidosYFecha() {
        val dv = RutUtils.calculateDv("20123456")
        val payload = "CHL|20123456-$dv|GONZALEZ|SILVA|LUCAS|IGNACIO|15/04/2008"
        val result = CedulaChileParser.parse(payload)

        assertNotNull(result)
        assertEquals(RutUtils.formatRut("20123456$dv"), result?.rut)
        assertEquals("Gonzalez Silva", result?.apellidos)
        assertEquals("Lucas Ignacio", result?.nombres)
        assertEquals("15/04/2008", result?.fechaNacimiento)
        assertEquals("PDF417", result?.sourceFormat)
    }

    @Test
    fun parse_pdf417ConMrz_extraeNombresDesdeBloqueDobleMenorQue() {
        val dv = RutUtils.calculateDv("19876543")
        val payload = "IDCHL0${"19876543"}$dv\nROJAS<MUÑOZ<<VALENTINA<PAZ<<<<<"
        val result = CedulaChileParser.parse(payload)

        assertNotNull(result)
        assertEquals(RutUtils.formatRut("19876543$dv"), result?.rut)
        assertEquals("Rojas Muñoz", result?.apellidos)
        assertEquals("Valentina Paz", result?.nombres)
    }

    @Test
    fun parse_inputsInvalidosOVacios_retornaNull() {
        assertNull(CedulaChileParser.parse(null))
        assertNull(CedulaChileParser.parse("   "))
        assertNull(CedulaChileParser.parse("https://portal.sidiv.registrocivil.cl/docstatus?RUN=11111111-9"))
        assertNull(CedulaChileParser.parse("TEXTO_SIN_RUT_VALIDO"))
    }
}
