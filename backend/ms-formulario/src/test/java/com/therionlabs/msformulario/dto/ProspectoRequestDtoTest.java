package com.therionlabs.msformulario.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProspectoRequestDtoTest {

    @Test
    void isContactoValidoEvaluaTodasLasCombinacionesDeCelularYEmail() {
        assertFalse(dtoWithContact(null, null).isContactoValido());
        assertFalse(dtoWithContact("   ", "").isContactoValido());
        assertTrue(dtoWithContact("+56987654321", null).isContactoValido());
        assertTrue(dtoWithContact(null, "test@duoc.cl").isContactoValido());
        assertTrue(dtoWithContact("  ", "test@duoc.cl").isContactoValido());
        assertTrue(dtoWithContact("+56987654321", "test@duoc.cl").isContactoValido());
    }

    private ProspectoRequestDto dtoWithContact(String celular, String email) {
        return new ProspectoRequestDto(
                "uuid-1", 1, "EVT-1", 1728834720000L,
                "11.111.111-1", "Camila", "Soto", "FEMENINO", "2008-04-15",
                "Viña del Mar", celular, email, true,
                "CUARTO_MEDIO", null, "Colegio", "Viña del Mar",
                "DIURNA", "Viña del Mar", "ALTO", "VOCACIONAL",
                List.of(new CarreraPreferenciaDto(1, "Informática"))
        );
    }
}
