package com.therionlabs.mslogin.dto;

import com.therionlabs.mslogin.model.Rol;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void aceptaUnUsuarioCompleto() {
        Set<ConstraintViolation<UsuarioRequest>> errores =
                validator.validate(new UsuarioRequest("Nuevo", "nuevo@captura.cl", "123456", Rol.CAPTADOR));

        assertTrue(errores.isEmpty());
    }

    @Test
    void rechazaNombreVacio() {
        Set<ConstraintViolation<UsuarioRequest>> errores =
                validator.validate(new UsuarioRequest("", "nuevo@captura.cl", "123456", Rol.CAPTADOR));

        assertEquals(1, errores.size());
        assertEquals("nombre", errores.iterator().next().getPropertyPath().toString());
    }

    @Test
    void rechazaCorreoInvalido() {
        Set<ConstraintViolation<UsuarioRequest>> errores =
                validator.validate(new UsuarioRequest("Nuevo", "malo", "123456", Rol.CAPTADOR));

        assertEquals(1, errores.size());
        assertEquals("email", errores.iterator().next().getPropertyPath().toString());
    }

    @Test
    void rechazaContrasenaConMenosDeSeisCaracteres() {
        Set<ConstraintViolation<UsuarioRequest>> errores =
                validator.validate(new UsuarioRequest("Nuevo", "nuevo@captura.cl", "12345", Rol.CAPTADOR));

        assertEquals(1, errores.size());
        assertEquals("password", errores.iterator().next().getPropertyPath().toString());
    }

    @Test
    void rechazaRolNulo() {
        Set<ConstraintViolation<UsuarioRequest>> errores =
                validator.validate(new UsuarioRequest("Nuevo", "nuevo@captura.cl", "123456", null));

        assertEquals(1, errores.size());
        assertEquals("rol", errores.iterator().next().getPropertyPath().toString());
    }
}
