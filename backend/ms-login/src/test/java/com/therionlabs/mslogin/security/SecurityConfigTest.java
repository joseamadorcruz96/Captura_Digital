package com.therionlabs.mslogin.security;

import com.therionlabs.mslogin.dto.UsuarioRequest;
import com.therionlabs.mslogin.dto.UsuarioResponse;
import com.therionlabs.mslogin.model.Rol;
import com.therionlabs.mslogin.repository.UsuarioRepository;
import com.therionlabs.mslogin.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    private static final String BODY_USUARIO =
            "{\"nombre\":\"Nuevo\",\"email\":\"nuevo@captura.cl\",\"password\":\"123456\",\"rol\":\"CAPTADOR\"}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        usuarioService.crear(new UsuarioRequest("Administrador", "admin@captura.cl", "123456", Rol.ADMINISTRATIVO));
    }

    private String token(Rol rol) {
        return "Bearer " + jwtService.generar(new UsuarioResponse(1L, "Prueba", "prueba@captura.cl", rol, true));
    }

    @Test
    void loginEsPublicoYDevuelveToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@captura.cl\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.usuario.rol").value("ADMINISTRATIVO"));
    }

    @Test
    void loginConContrasenaIncorrectaDevuelve401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@captura.cl\",\"password\":\"000000\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void recuperarEsPublico() throws Exception {
        mockMvc.perform(post("/api/auth/recuperar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@captura.cl\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void usuariosSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuariosConTokenInvalidoDevuelve401() throws Exception {
        mockMvc.perform(get("/api/usuarios").header("Authorization", "Bearer token-falso"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuariosConRolCaptadorDevuelve403() throws Exception {
        mockMvc.perform(get("/api/usuarios").header("Authorization", token(Rol.CAPTADOR)))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuariosConRolProspectoDevuelve403() throws Exception {
        mockMvc.perform(get("/api/usuarios").header("Authorization", token(Rol.PROSPECTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuariosConRolAdministrativoDevuelve200() throws Exception {
        mockMvc.perform(get("/api/usuarios").header("Authorization", token(Rol.ADMINISTRATIVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("admin@captura.cl"));
    }

    @Test
    void crearUsuarioSoloLoPuedeHacerUnAdministrativo() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", token(Rol.CAPTADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_USUARIO))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", token(Rol.ADMINISTRATIVO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_USUARIO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("nuevo@captura.cl"));
    }

    @Test
    void cualquierOtraRutaSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/otra-ruta"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cualquierOtraRutaConTokenPasaLaSeguridad() throws Exception {
        mockMvc.perform(get("/api/otra-ruta").header("Authorization", token(Rol.CAPTADOR)))
                .andExpect(status().isNotFound());
    }
}
