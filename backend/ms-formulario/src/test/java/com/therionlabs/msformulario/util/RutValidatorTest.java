package com.therionlabs.msformulario.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RutValidatorTest {

    @Test
    void validatesAndNormalizesValidRutsIncludingKAndZero() {
        // Dígito verificador numérico (11.111.111-1)
        assertTrue(RutValidator.isValid("111111111"));
        assertTrue(RutValidator.isValid("11.111.111-1"));
        assertEquals("11.111.111-1", RutValidator.normalize("111111111"));

        // Dígito verificador K (11.111.112-K)
        assertTrue(RutValidator.isValid("11111112k"));
        assertEquals("11.111.112-K", RutValidator.normalize("11.111.112-k"));

        // Dígito verificador 0 (11.111.117-0)
        assertTrue(RutValidator.isValid("11.111.117-0"));
        assertEquals("11.111.117-0", RutValidator.normalize("111111170"));
    }

    @Test
    void rejectsInvalidRuts() {
        assertFalse(RutValidator.isValid("11.111.111-9"));
        assertFalse(RutValidator.isValid("12345"));
        assertFalse(RutValidator.isValid("1234567890"));
        assertFalse(RutValidator.isValid("ab.cde.fgh-1"));
        assertFalse(RutValidator.isValid(""));
        assertFalse(RutValidator.isValid(null));
    }

    @Test
    void normalizeHandlesShortAndLongInputs() {
        assertEquals("", RutValidator.normalize(null));
        assertEquals("12345", RutValidator.normalize("12345"));
        assertEquals("11.111.111-1", RutValidator.normalize("11111111199"));
    }
}
