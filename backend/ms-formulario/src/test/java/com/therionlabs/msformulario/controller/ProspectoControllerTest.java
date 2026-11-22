package com.therionlabs.msformulario.controller;

import com.therionlabs.msformulario.dto.BatchSyncRequestDto;
import com.therionlabs.msformulario.dto.CarreraPreferenciaDto;
import com.therionlabs.msformulario.dto.EventoRequestDto;
import com.therionlabs.msformulario.dto.ProspectoRequestDto;
import com.therionlabs.msformulario.repository.EventoRepository;
import com.therionlabs.msformulario.repository.ProspectoRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProspectoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProspectoRepository prospectoRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @BeforeEach
    void cleanDb() {
        prospectoRepository.deleteAll();
        eventoRepository.deleteAll();
    }

    @Test
    void endpointsProtegidosRechazanSinTokenTokenInvalidoYRolInsuficiente() throws Exception {
        mockMvc.perform(get("/api/v1/prospectos"))
                .andExpect(status().isUnauthorized());

        String tokenFirmaFalsa = signToken("atacante@evil.cl", "ADMINISTRATIVO",
                "otra-clave-secreta-distinta-de-32-bytes!");
        mockMvc.perform(get("/api/v1/prospectos")
                        .header("Authorization", "Bearer " + tokenFirmaFalsa))
                .andExpect(status().isUnauthorized());

        String tokenProspecto = signToken("postulante@duoc.cl", "PROSPECTO", jwtSecret);
        mockMvc.perform(get("/api/v1/prospectos")
                        .header("Authorization", "Bearer " + tokenProspecto))
                .andExpect(status().isForbidden());

        EventoRequestDto evento = new EventoRequestDto(
                "EVT-TEST", "Feria Duoc", "Liceo A", "Viña del Mar", "Viña del Mar", "2026-11-22", true);

        String tokenCaptador = signToken("captador@captura.cl", "CAPTADOR", jwtSecret);
        mockMvc.perform(post("/api/v1/eventos")
                        .header("Authorization", "Bearer " + tokenCaptador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(evento)))
                .andExpect(status().isForbidden());
    }

    @Test
    void flujoCompletoCrudYBatchSyncFuncionaConJwtValido() throws Exception {
        String adminToken = signToken("admin@captura.cl", "ADMINISTRATIVO", jwtSecret);
        String captadorToken = signToken("captador@captura.cl", "CAPTADOR", jwtSecret);

        EventoRequestDto evento = new EventoRequestDto(
                "EVT-TEST", "Feria Duoc", "Liceo A", "Viña del Mar", "Viña del Mar", "2026-11-22", true);

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(evento)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("EVT-TEST"));

        ProspectoRequestDto p1 = sampleDto("uuid-p1", "111111111", "c1@gmail.com");

        mockMvc.perform(post("/api/v1/prospectos")
                        .header("Authorization", "Bearer " + captadorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rut").value("11.111.111-1"));

        mockMvc.perform(post("/api/v1/prospectos")
                        .header("Authorization", "Bearer " + captadorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDto("uuid-otro", "11.111.111-1", "c2@gmail.com"))))
                .andExpect(status().isConflict());

        mockMvc.perform(put("/api/v1/prospectos/uuid-p1")
                        .header("Authorization", "Bearer " + captadorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDto("uuid-p1", "11.111.111-1", "actualizado@gmail.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("actualizado@gmail.com"));

        BatchSyncRequestDto syncBatch = new BatchSyncRequestDto(
                2,
                "EVT-TEST",
                List.of(
                        sampleDto("uuid-p1-reemplazo", "11.111.111-1", "upsert@gmail.com"),
                        sampleDto("uuid-p2", "11111112k", "nuevo@gmail.com"),
                        sampleDto("uuid-malo", "11.111.111-9", "malo@gmail.com")
                )
        );

        mockMvc.perform(post("/api/v1/prospectos/sync")
                        .header("Authorization", "Bearer " + captadorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncBatch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProcesados").value(3))
                .andExpect(jsonPath("$.exitososUuids", containsInAnyOrder("uuid-p1-reemplazo", "uuid-p2")))
                .andExpect(jsonPath("$.fallidosUuids", hasSize(1)))
                .andExpect(jsonPath("$.fallidosUuids[0]").value("uuid-malo"));

        mockMvc.perform(get("/api/v1/prospectos/resumen")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProspectos").value(2));

        mockMvc.perform(delete("/api/v1/prospectos/uuid-p2")
                        .header("Authorization", "Bearer " + captadorToken))
                .andExpect(status().isNoContent());
    }

    private String signToken(String email, String rol, String secret) {
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .claim("rol", rol)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 3600_000L))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private ProspectoRequestDto sampleDto(String uuid, String rut, String email) {
        return new ProspectoRequestDto(
                uuid, 2, "EVT-TEST", 1728834720000L,
                rut, "Camila", "Valenzuela", "FEMENINO", "2008-04-15",
                "Quilpué", "+56987654321", email, true,
                "CUARTO_MEDIO", null, "Colegio Alemán", "Viña del Mar",
                "DIURNA", "Viña del Mar", "ALTO", "VOCACIONAL",
                List.of(new CarreraPreferenciaDto(1, "Diseño Gráfico"))
        );
    }
}
