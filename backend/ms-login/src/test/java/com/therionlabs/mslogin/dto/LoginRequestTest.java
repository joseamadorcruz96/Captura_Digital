package com.therionlabs.mslogin.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void aceptaCorreoYContrasenaValidos() {
        Set<ConstraintViolation<LoginRequest>> errores =
                validator.validate(new LoginRequest("admin@captura.cl", "123456"));

        assertTrue(errores.isEmpty());
    }

    @Test
    void rechazaCorreoConFormatoInvalido() {
        Set<ConstraintViolation<LoginRequest>> errores =
                validator.validate(new LoginRequest("no-es-correo", "123456"));

        assertEquals(1, errores.size());
        assertEquals("email", errores.iterator().next().getPropertyPath().toString());
    }

    @Test
    void rechazaCorreoYContrasenaVacios() {
        Set<ConstraintViolation<LoginRequest>> errores =
                validator.validate(new LoginRequest("", ""));

        assertEquals(2, errores.size());
    }

    @Test
    void rechazaValoresNulos() {
        Set<ConstraintViolation<LoginRequest>> errores =
                validator.validate(new LoginRequest(null, null));

        assertEquals(2, errores.size());
    }
}
