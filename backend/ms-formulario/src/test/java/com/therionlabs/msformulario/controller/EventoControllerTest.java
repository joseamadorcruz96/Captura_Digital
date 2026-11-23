package com.therionlabs.msformulario.controller;

import com.therionlabs.msformulario.dto.EventoRequestDto;
import com.therionlabs.msformulario.dto.EventoResponseDto;
import com.therionlabs.msformulario.exception.ApiException;
import com.therionlabs.msformulario.exception.GlobalExceptionHandler;
import com.therionlabs.msformulario.service.EventoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EventoControllerTest {

    @Mock
    private EventoService eventoService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(new EventoController(eventoService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listarEventosSinFiltroYConFiltroActivo() throws Exception {
        EventoResponseDto e1 = sampleResponse("EVT-1", true);
        EventoResponseDto e2 = sampleResponse("EVT-2", false);
        when(eventoService.listar(null)).thenReturn(List.of(e1, e2));
        when(eventoService.listar(true)).thenReturn(List.of(e1));

        mockMvc.perform(get("/api/v1/eventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/v1/eventos").param("activo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("EVT-1"));
    }

    @Test
    void crearEventoRetorna201YManejaValidacionYConflict() throws Exception {
        EventoRequestDto valido = new EventoRequestDto(
                "EVT-1", "Feria Duoc", "Liceo A", "Viña del Mar", "Viña del Mar", "2026-11-22", true);
        when(eventoService.crear(any())).thenReturn(sampleResponse("EVT-1", true));

        mockMvc.perform(post("/api/v1/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valido)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("EVT-1"));

        EventoRequestDto invalido = new EventoRequestDto("EVT-1", "", "", "", "", null, true);
        mockMvc.perform(post("/api/v1/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nombre").exists());

        when(eventoService.crear(any())).thenThrow(ApiException.conflict("Ya existe"));
        mockMvc.perform(post("/api/v1/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valido)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ya existe"));
    }

    @Test
    void actualizarEventoRetorna200Y404() throws Exception {
        EventoRequestDto req = new EventoRequestDto(
                "EVT-1", "Feria Editada", "Liceo B", "Valparaíso", "Valparaíso", "2026-12-01", false);
        when(eventoService.actualizar(eq("EVT-1"), any())).thenReturn(sampleResponse("EVT-1", false));

        mockMvc.perform(put("/api/v1/eventos/EVT-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        when(eventoService.actualizar(eq("EVT-404"), any()))
                .thenThrow(ApiException.notFound("Evento no encontrado: EVT-404"));
        mockMvc.perform(put("/api/v1/eventos/EVT-404")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarEventoRetorna204Y404() throws Exception {
        doNothing().when(eventoService).eliminar("EVT-1");

        mockMvc.perform(delete("/api/v1/eventos/EVT-1"))
                .andExpect(status().isNoContent());
        verify(eventoService).eliminar("EVT-1");

        doThrow(ApiException.notFound("Evento no encontrado: EVT-404"))
                .when(eventoService).eliminar("EVT-404");

        mockMvc.perform(delete("/api/v1/eventos/EVT-404"))
                .andExpect(status().isNotFound());
    }

    private EventoResponseDto sampleResponse(String id, boolean activo) {
        return new EventoResponseDto(
                id, "Feria Duoc", "Liceo A", "Viña del Mar", "Viña del Mar", "2026-11-22", activo, Instant.now());
    }
}
