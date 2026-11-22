package com.therionlabs.msformulario.service;

import com.therionlabs.msformulario.dto.CarreraPreferenciaDto;
import com.therionlabs.msformulario.dto.ProspectoRequestDto;
import com.therionlabs.msformulario.exception.ApiException;
import com.therionlabs.msformulario.mapper.ProspectoMapper;
import com.therionlabs.msformulario.model.Prospecto;
import com.therionlabs.msformulario.repository.ProspectoRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProspectoSyncItemProcessorTest {

    @Mock
    private ProspectoRepository prospectoRepository;

    private ProspectoSyncItemProcessor processor;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        processor = new ProspectoSyncItemProcessor(prospectoRepository, new ProspectoMapper(), validator);
    }

    @Test
    void procesarItemInsertaNuevoCuandoRutNoExiste() {
        ProspectoRequestDto dto = sampleDto("uuid-captador-a", "111111111");
        when(prospectoRepository.findByRut("11.111.111-1")).thenReturn(Optional.empty());
        when(prospectoRepository.saveAndFlush(any(Prospecto.class))).thenAnswer(inv -> inv.getArgument(0));

        String confirmedUuid = processor.procesarItem(dto);

        assertEquals("uuid-captador-a", confirmedUuid);
        verify(prospectoRepository).saveAndFlush(any(Prospecto.class));
    }

    @Test
    void procesarItemHaceUpsertYDevuelveUuidDelCaptadorEmisor() {
        Prospecto existente = Prospecto.builder()
                .clientUuid("uuid-captador-a")
                .rut("11.111.111-1")
                .build();
        ProspectoRequestDto dtoDeCaptadorB = sampleDto("uuid-captador-b", "11.111.111-1");

        when(prospectoRepository.findByRut("11.111.111-1")).thenReturn(Optional.of(existente));
        when(prospectoRepository.saveAndFlush(any(Prospecto.class))).thenAnswer(inv -> inv.getArgument(0));

        String confirmedUuid = processor.procesarItem(dtoDeCaptadorB);

        // Debe devolver el UUID del dispositivo B para desbloquear su estado PENDING_SYNC en Room
        assertEquals("uuid-captador-b", confirmedUuid);
        assertEquals("uuid-captador-a", existente.getClientUuid());
    }

    @Test
    void procesarItemRechazaRutInvalidoYEmailInvalido() {
        ProspectoRequestDto rutMalo = sampleDto("uuid-1", "11.111.111-9");
        assertThrows(ApiException.class, () -> processor.procesarItem(rutMalo));

        ProspectoRequestDto emailMalo = new ProspectoRequestDto(
                "uuid-2", 1, "EVT-1", 1728834720000L,
                "11.111.111-1", "Camila", "Soto", "FEMENINO", "2008-04-15",
                "Viña del Mar", "+56987654321", "correo-invalido", true,
                "CUARTO_MEDIO", null, "Colegio Alemán", "Viña del Mar",
                "DIURNA", "Viña del Mar", "ALTO", "VOCACIONAL",
                List.of(new CarreraPreferenciaDto(1, "Ingeniería en Informática"))
        );
        assertThrows(ApiException.class, () -> processor.procesarItem(emailMalo));
        verifyNoInteractions(prospectoRepository);
    }

    private ProspectoRequestDto sampleDto(String uuid, String rut) {
        return new ProspectoRequestDto(
                uuid, 2, "EVT-1", 1728834720000L,
                rut, "Camila", "Valenzuela", "FEMENINO", "2008-04-15",
                "Quilpué", "+56987654321", "c.valenzuela@gmail.com", true,
                "CUARTO_MEDIO", null, "Colegio Alemán", "Viña del Mar",
                "DIURNA", "Viña del Mar", "ALTO", "VOCACIONAL",
                List.of(new CarreraPreferenciaDto(1, "Diseño Gráfico"))
        );
    }
}
