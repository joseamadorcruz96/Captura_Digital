package com.therionlabs.mslogin.controller;

import com.therionlabs.mslogin.dto.UsuarioRequest;
import com.therionlabs.mslogin.dto.UsuarioResponse;
import com.therionlabs.mslogin.model.Rol;
import com.therionlabs.mslogin.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UsuarioControllerTest {

    private static final String BODY_VALIDO =
            "{\"nombre\":\"Nuevo\",\"email\":\"nuevo@captura.cl\",\"password\":\"123456\",\"rol\":\"CAPTADOR\"}";

    private UsuarioService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(UsuarioService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new UsuarioController(service)).build();
    }

    @Test
    void crearDevuelve201ConElUsuarioCreado() throws Exception {
        when(service.crear(any(UsuarioRequest.class)))
                .thenReturn(new UsuarioResponse(1L, "Nuevo", "nuevo@captura.cl", Rol.CAPTADOR, true));

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("nuevo@captura.cl"))
                .andExpect(jsonPath("$.rol").value("CAPTADOR"));
    }

    @Test
    void crearConDatosInvalidosDevuelve400() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"email\":\"malo\",\"password\":\"123\",\"rol\":\"CAPTADOR\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearConRolInexistenteDevuelve400() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nuevo\",\"email\":\"nuevo@captura.cl\",\"password\":\"123456\",\"rol\":\"INVITADO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearConCorreoRepetidoDevuelve409() throws Exception {
        when(service.crear(any(UsuarioRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya esta registrado"));

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isConflict());
    }

    @Test
    void listarDevuelveTodosLosUsuarios() throws Exception {
        when(service.listar()).thenReturn(List.of(
                new UsuarioResponse(1L, "Administrador", "admin@captura.cl", Rol.ADMINISTRATIVO, true),
                new UsuarioResponse(2L, "Captador", "captador@captura.cl", Rol.CAPTADOR, true)));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].rol").value("CAPTADOR"));
    }

    @Test
    void obtenerDevuelveElUsuarioONotFound() throws Exception {
        when(service.obtener(1L))
                .thenReturn(new UsuarioResponse(1L, "Administrador", "admin@captura.cl", Rol.ADMINISTRATIVO, true));
        when(service.obtener(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@captura.cl"));

        mockMvc.perform(get("/api/usuarios/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarDevuelve200ONotFound() throws Exception {
        when(service.actualizar(eq(1L), any(UsuarioRequest.class)))
                .thenReturn(new UsuarioResponse(1L, "Nuevo", "nuevo@captura.cl", Rol.CAPTADOR, true));
        when(service.actualizar(eq(99L), any(UsuarioRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nuevo"));

        mockMvc.perform(put("/api/usuarios/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarDevuelve204ONotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"))
                .when(service).eliminar(99L);

        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isNoContent());
        verify(service).eliminar(1L);

        mockMvc.perform(delete("/api/usuarios/99"))
                .andExpect(status().isNotFound());
    }
}
