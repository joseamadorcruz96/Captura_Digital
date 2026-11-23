package com.therionlabs.msformulario.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        mockMvc = MockMvcBuilders.standaloneSetup(new DummyController())
                .setControllerAdvice(handler)
                .build();
    }

    @Test
    void manejaApiExceptionYIllegalArgumentException() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/test");

        ResponseEntity<ApiErrorResponse> apiResp = handler.handleApiException(
                ApiException.conflict("RUT duplicado"), request);
        assertEquals(HttpStatus.CONFLICT, apiResp.getStatusCode());
        assertNotNull(apiResp.getBody());
        assertEquals("RUT duplicado", apiResp.getBody().message());
        assertEquals("/api/v1/test", apiResp.getBody().path());
        assertNull(apiResp.getBody().fieldErrors());

        ResponseEntity<ApiErrorResponse> badResp = handler.handleBadInput(
                new IllegalArgumentException("Valor inválido"), request);
        assertEquals(HttpStatus.BAD_REQUEST, badResp.getStatusCode());
        assertEquals("Valor inválido", badResp.getBody().message());
    }

    @Test
    void manejaMethodArgumentNotValidConMultiplesErroresPorCampo() throws Exception {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new DummyBody(""), "dummy");
        binding.addError(new FieldError("dummy", "nombre", "primer error"));
        binding.addError(new FieldError("dummy", "nombre", "segundo error"));
        Method m = DummyController.class.getDeclaredMethod("validar", DummyBody.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(m, 0), binding);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/dummy");
        ResponseEntity<ApiErrorResponse> resp = handler.handleValidation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("primer error", resp.getBody().fieldErrors().get("nombre"));
    }

    @Test
    void manejaHttpMessageNotReadableYError500ViaMockMvc() throws Exception {
        mockMvc.perform(post("/dummy/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{json-malo"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(get("/dummy/illegal"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Argumento inválido"));

        mockMvc.perform(get("/dummy/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Error interno del servidor al procesar la solicitud"));
    }

    record DummyBody(@NotBlank String nombre) {}

    @RestController
    @RequestMapping("/dummy")
    static class DummyController {
        @PostMapping("/validar")
        public String validar(@Valid @RequestBody DummyBody body) {
            return body.nombre();
        }

        @GetMapping("/illegal")
        public String illegal() {
            throw new IllegalArgumentException("Argumento inválido");
        }

        @GetMapping("/boom")
        public String boom() {
            throw new RuntimeException("Falla inesperada");
        }
    }
}
