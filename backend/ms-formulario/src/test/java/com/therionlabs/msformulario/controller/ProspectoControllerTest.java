package com.therionlabs.msformulario.controller;

import com.therionlabs.msformulario.dto.BatchSyncRequestDto;
import com.therionlabs.msformulario.dto.CarreraPreferenciaDto;
import com.therionlabs.msformulario.dto.EventoRequestDto;
import com.therionlabs.msformulario.dto.ProspectoRequestDto;
import com.therionlabs.msformulario.repository.EventoRepository;
import com.therionlabs.msformulario.repository.ProspectoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

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

    @BeforeEach
    void cleanDb() {
        prospectoRepository.deleteAll();
        eventoRepository.deleteAll();
    }

    @Test
    void endpointsProtegidosRechazanSinCredencialesYPorRolInsuficiente() throws Exception {
        mockMvc.perform(get("/api/v1/prospectos"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/prospectos")
                        .header("X-User-Id", "99")
                        .header("X-User-Rol", "PROSPECTO"))
                .andExpect(status().isForbidden());

        EventoRequestDto evento = new EventoRequestDto(
                "EVT-TEST", "Feria Duoc", "Liceo A", "Viña del Mar", "Viña del Mar", "2026-11-22", true);

        mockMvc.perform(post("/api/v1/eventos")
                        .header("X-User-Id", "2")
                        .header("X-User-Rol", "CAPTADOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(evento)))
                .andExpect(status().isForbidden());
    }

    @Test
    void flujoCompletoCrudYBatchSyncFuncionaConRolesValidos() throws Exception {
        EventoRequestDto evento = new EventoRequestDto(
                "EVT-TEST", "Feria Duoc", "Liceo A", "Viña del Mar", "Viña del Mar", "2026-11-22", true);

        mockMvc.perform(post("/api/v1/eventos")
                        .header("X-User-Id", "1")
                        .header("X-User-Rol", "ADMINISTRATIVO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(evento)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("EVT-TEST"));

        ProspectoRequestDto p1 = sampleDto("uuid-p1", "111111111", "c1@gmail.com");

        mockMvc.perform(post("/api/v1/prospectos")
                        .header("X-User-Id", "2")
                        .header("X-User-Rol", "CAPTADOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rut").value("11.111.111-1"));

        // Duplicado en POST unitario debe responder 409
        mockMvc.perform(post("/api/v1/prospectos")
                        .header("X-User-Id", "2")
                        .header("X-User-Rol", "CAPTADOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDto("uuid-otro", "11.111.111-1", "c2@gmail.com"))))
                .andExpect(status().isConflict());

        // Actualizar por clientUuid
        mockMvc.perform(put("/api/v1/prospectos/uuid-p1")
                        .header("X-User-Id", "2")
                        .header("X-User-Rol", "CAPTADOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDto("uuid-p1", "11.111.111-1", "actualizado@gmail.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("actualizado@gmail.com"));

        // Batch Sync parcial: 1 upsert (p1 con otro uuid) + 1 nuevo válido + 1 inválido
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
                        .header("X-User-Id", "2")
                        .header("X-User-Rol", "CAPTADOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncBatch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProcesados").value(3))
                .andExpect(jsonPath("$.exitososUuids", containsInAnyOrder("uuid-p1-reemplazo", "uuid-p2")))
                .andExpect(jsonPath("$.fallidosUuids", hasSize(1)))
                .andExpect(jsonPath("$.fallidosUuids[0]").value("uuid-malo"));

        // Resumen Dashboard
        mockMvc.perform(get("/api/v1/prospectos/resumen")
                        .header("X-User-Id", "1")
                        .header("X-User-Rol", "ADMINISTRATIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProspectos").value(2));

        // Eliminar
        mockMvc.perform(delete("/api/v1/prospectos/uuid-p2")
                        .header("X-User-Id", "2")
                        .header("X-User-Rol", "CAPTADOR"))
                .andExpect(status().isNoContent());
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
