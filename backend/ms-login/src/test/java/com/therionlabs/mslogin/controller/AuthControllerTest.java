package com.therionlabs.mslogin.controller;

import com.therionlabs.mslogin.dto.LoginRequest;
import com.therionlabs.mslogin.dto.UsuarioResponse;
import com.therionlabs.mslogin.model.Rol;
import com.therionlabs.mslogin.security.JwtService;
import com.therionlabs.mslogin.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private static final String SECRET = "clave-de-prueba-para-ms-login-con-mas-de-32-bytes";

    private UsuarioService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(UsuarioService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(service, new JwtService(SECRET))).build();
    }

    @Test
    void loginCorrectoDevuelveTokenYUsuario() throws Exception {
        UsuarioResponse usuario = new UsuarioResponse(1L, "Administrador", "admin@captura.cl", Rol.ADMINISTRATIVO, true);
        when(service.login(any(LoginRequest.class))).thenReturn(usuario);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@captura.cl\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.usuario.email").value("admin@captura.cl"))
                .andExpect(jsonPath("$.usuario.rol").value("ADMINISTRATIVO"))
                .andExpect(jsonPath("$.usuario.activo").value(true));
    }

    @Test
    void loginConCredencialesIncorrectasDevuelve401() throws Exception {
        when(service.login(any(LoginRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contrasena incorrectos"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@captura.cl\",\"password\":\"000000\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginConDatosInvalidosDevuelve400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"no-es-correo\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void recuperarConCorreoRegistradoDevuelveMensaje() throws Exception {
        mockMvc.perform(post("/api/auth/recuperar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@captura.cl\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Enviamos las instrucciones a admin@captura.cl"));

        verify(service).recuperar("admin@captura.cl");
    }

    @Test
    void recuperarConCorreoNoRegistradoDevuelve404() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Este correo no esta registrado"))
                .when(service).recuperar("nadie@captura.cl");

        mockMvc.perform(post("/api/auth/recuperar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nadie@captura.cl\"}"))
                .andExpect(status().isNotFound());
    }
}
