package com.therionlabs.msformulario.mapper;

import com.therionlabs.msformulario.dto.CarreraPreferenciaDto;
import com.therionlabs.msformulario.dto.EventoRequestDto;
import com.therionlabs.msformulario.dto.EventoResponseDto;
import com.therionlabs.msformulario.dto.ProspectoRequestDto;
import com.therionlabs.msformulario.dto.ProspectoResponseDto;
import com.therionlabs.msformulario.model.Evento;
import com.therionlabs.msformulario.model.Prospecto;
import com.therionlabs.msformulario.model.enums.Genero;
import com.therionlabs.msformulario.model.enums.Jornada;
import com.therionlabs.msformulario.model.enums.NivelInteres;
import com.therionlabs.msformulario.model.enums.PerfilMotivacional;
import com.therionlabs.msformulario.model.enums.SituacionAcademica;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProspectoMapperTest {

    private ProspectoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProspectoMapper();
    }

    @Test
    void toNewEntityNormalizaCamposYHandlesNullsYClientUuid() {
        ProspectoRequestDto conOpcionalesNulos = new ProspectoRequestDto(
                "   ", 5, null, null,
                "111111111", "  Camila ", " Soto  ", " femenino ", "2008-04-15",
                " Viña del Mar ", null, null, true,
                " cuarto_medio ", null, " Liceo A ", " Viña ",
                " diurna ", " Viña del Mar ", " alto ", " vocacional ",
                List.of(new CarreraPreferenciaDto(1, "  Diseño Gráfico  "))
        );

        Prospecto entity = mapper.toNewEntity(conOpcionalesNulos);

        assertNull(entity.getClientUuid());
        assertNotNull(entity.getTimestampEpoch());
        assertEquals("11.111.111-1", entity.getRut());
        assertEquals("Camila", entity.getNombres());
        assertEquals("Soto", entity.getApellidos());
        assertEquals(Genero.FEMENINO, entity.getGenero());
        assertEquals("", entity.getCelular());
        assertEquals("", entity.getEmail());
        assertEquals(SituacionAcademica.CUARTO_MEDIO, entity.getSituacionAcademica());
        assertEquals(Jornada.DIURNA, entity.getJornadaPreferencia());
        assertEquals(NivelInteres.ALTO, entity.getNivelInteres());
        assertEquals(PerfilMotivacional.VOCACIONAL, entity.getPerfilMotivacional());
        assertEquals("Diseño Gráfico", entity.getCarreras().get(0).getNombreCarrera());

        ProspectoRequestDto conUuidYContacto = new ProspectoRequestDto(
                "  uuid-abc  ", 5, "EVT-1", 1728834720000L,
                "11.111.111-1", "Camila", "Soto", "FEMENINO", "2008-04-15",
                "Viña del Mar", " +56912345678 ", "  CAMILA@DUOC.CL  ", true,
                "CUARTO_MEDIO", null, "Liceo A", "Viña",
                "VESPERTINA", "Viña del Mar", "MEDIO", "INVERSION",
                List.of(new CarreraPreferenciaDto(1, "Informática"))
        );

        Prospecto entity2 = mapper.toNewEntity(conUuidYContacto);
        assertEquals("uuid-abc", entity2.getClientUuid());
        assertEquals(1728834720000L, entity2.getTimestampEpoch());
        assertEquals("+56912345678", entity2.getCelular());
        assertEquals("camila@duoc.cl", entity2.getEmail());

        entity2.setCreatedAt(Instant.now());
        entity2.setUpdatedAt(Instant.now());
        ProspectoResponseDto dto = mapper.toResponseDto(entity2);
        assertEquals("VESPERTINA", dto.jornadaPreferencia());
        assertEquals("Informática", dto.carreras().get(0).nombreCarrera());
    }

    @Test
    void parseEnumLanzaIllegalArgumentParaNuloBlancoEInvalido() {
        ProspectoRequestDto enumNulo = new ProspectoRequestDto(
                null, 1, null, null,
                "111111111", "Ana", "Pérez", null, null,
                "Quilpué", "+56911111111", null, true,
                "CUARTO_MEDIO", null, "Liceo", "Quilpué",
                "DIURNA", "Viña", "ALTO", "VOCACIONAL",
                List.of(new CarreraPreferenciaDto(1, "Carrera"))
        );
        assertThrows(IllegalArgumentException.class, () -> mapper.toNewEntity(enumNulo));

        ProspectoRequestDto enumBlanco = new ProspectoRequestDto(
                null, 1, null, null,
                "111111111", "Ana", "Pérez", "  ", null,
                "Quilpué", "+56911111111", null, true,
                "CUARTO_MEDIO", null, "Liceo", "Quilpué",
                "DIURNA", "Viña", "ALTO", "VOCACIONAL",
                List.of(new CarreraPreferenciaDto(1, "Carrera"))
        );
        assertThrows(IllegalArgumentException.class, () -> mapper.toNewEntity(enumBlanco));

        ProspectoRequestDto enumInvalido = new ProspectoRequestDto(
                null, 1, null, null,
                "111111111", "Ana", "Pérez", "OTRO_RARO", null,
                "Quilpué", "+56911111111", null, true,
                "CUARTO_MEDIO", null, "Liceo", "Quilpué",
                "DIURNA", "Viña", "ALTO", "VOCACIONAL",
                List.of(new CarreraPreferenciaDto(1, "Carrera"))
        );
        assertThrows(IllegalArgumentException.class, () -> mapper.toNewEntity(enumInvalido));
    }

    @Test
    void mapeaEventosConIdYActivoOpcionales() {
        EventoRequestDto sinIdNiActivo = new EventoRequestDto(
                "  ", " Feria ", " Liceo ", " Valpo ", " Valparaíso ", "2026-11-22", null);

        Evento evento = mapper.toEventoEntity(sinIdNiActivo);
        assertNull(evento.getId());
        assertTrue(evento.isActivo());
        assertEquals("Feria", evento.getNombre());

        EventoRequestDto conIdYFalse = new EventoRequestDto(
                " EVT-9 ", "Feria 2", "Liceo 2", "Viña", "Viña del Mar", "2026-12-01", false);
        Evento evento2 = mapper.toEventoEntity(conIdYFalse);
        assertEquals("EVT-9", evento2.getId());
        assertFalse(evento2.isActivo());

        EventoResponseDto response = mapper.toEventoResponseDto(evento2);
        assertEquals("EVT-9", response.id());
        assertFalse(response.activo());
    }
}
