package com.therionlabs.captura_digital_v2

import com.therionlabs.captura_digital_v2.util.RutUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RutUtilsTest {

    @Test
    fun testValidRuts() {
        assertTrue(RutUtils.isValidRut("11.111.111-1"))
        assertTrue(RutUtils.isValidRut("111111111"))
        assertTrue(RutUtils.isValidRut("11111111-1"))
        assertTrue(RutUtils.isValidRut("30.686.957-4"))
        assertTrue(RutUtils.isValidRut("20.123.456-5"))
        assertTrue(RutUtils.isValidRut("10.000.030-K"))
        assertTrue(RutUtils.isValidRut("10000030k"))
    }

    @Test
    fun testInvalidRuts() {
        assertFalse(RutUtils.isValidRut("11.111.111-2"))
        assertFalse(RutUtils.isValidRut("12345"))
        assertFalse(RutUtils.isValidRut("abc-4"))
        assertFalse(RutUtils.isValidRut(""))
    }

    @Test
    fun testFormatRut() {
        assertEquals("11.111.111-1", RutUtils.formatRut("111111111"))
        assertEquals("20.123.456-5", RutUtils.formatRut("201234565"))
        assertEquals("10.000.030-K", RutUtils.formatRut("10000030k"))
    }
}
