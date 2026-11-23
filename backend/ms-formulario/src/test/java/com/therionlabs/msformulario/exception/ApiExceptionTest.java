package com.therionlabs.msformulario.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionTest {

    @Test
    void factoryMethodsConstruyenStatusYMensajeCorrectos() {
        ApiException bad = ApiException.badRequest("bad");
        assertEquals(HttpStatus.BAD_REQUEST, bad.getStatus());
        assertEquals("bad", bad.getMessage());

        ApiException notFound = ApiException.notFound("missing");
        assertEquals(HttpStatus.NOT_FOUND, notFound.getStatus());
        assertEquals("missing", notFound.getMessage());

        ApiException conflict = ApiException.conflict("dup");
        assertEquals(HttpStatus.CONFLICT, conflict.getStatus());
        assertEquals("dup", conflict.getMessage());
    }
}
