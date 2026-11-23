package com.therionlabs.msformulario.service;

import com.therionlabs.msformulario.dto.EventoRequestDto;
import com.therionlabs.msformulario.dto.EventoResponseDto;
import com.therionlabs.msformulario.exception.ApiException;
import com.therionlabs.msformulario.mapper.ProspectoMapper;
import com.therionlabs.msformulario.model.Evento;
import com.therionlabs.msformulario.repository.EventoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    @Mock
    private EventoRepository eventoRepository;

    private EventoService eventoService;

    @BeforeEach
    void setUp() {
        eventoService = new EventoService(eventoRepository, new ProspectoMapper());
    }

    @Test
    void listarSoloActivosConsultaRepositorioDeActivos() {
        Evento activo = sampleEvento("EVT-1", true);
        when(eventoRepository.findByActivoTrueOrderByCreatedAtDesc()).thenReturn(List.of(activo));

        List<EventoResponseDto> result = eventoService.listar(true);

        assertEquals(1, result.size());
        assertEquals("EVT-1", result.get(0).id());
        verify(eventoRepository).findByActivoTrueOrderByCreatedAtDesc();
        verify(eventoRepository, never()).findAll();
    }

    @Test
    void listarTodosCuandoActivoEsFalseONull() {
        Evento e1 = sampleEvento("EVT-1", true);
        Evento e2 = sampleEvento("EVT-2", false);
        when(eventoRepository.findAll()).thenReturn(List.of(e1, e2));

        List<EventoResponseDto> conNull = eventoService.listar(null);
        List<EventoResponseDto> conFalse = eventoService.listar(false);

        assertEquals(2, conNull.size());
        assertEquals(2, conFalse.size());
        verify(eventoRepository, never()).findByActivoTrueOrderByCreatedAtDesc();
    }

    @Test
    void crearGuardaEventoCuandoIdNoExiste() {
        EventoRequestDto request = new EventoRequestDto(
                " EVT-NEW ", "Feria Primavera", "Liceo A", "Viña del Mar", "Viña del Mar", "2026-11-22", true);
        when(eventoRepository.existsById("EVT-NEW")).thenReturn(false);
        when(eventoRepository.save(any(Evento.class))).thenAnswer(inv -> inv.getArgument(0));

        EventoResponseDto response = eventoService.crear(request);

        assertEquals("EVT-NEW", response.id());
        assertEquals("Feria Primavera", response.nombre());
    }

    @Test
    void crearGuardaEventoCuandoIdEsNuloOBlanco() {
        EventoRequestDto conIdNulo = new EventoRequestDto(
                null, "Feria Norte", "Colegio B", "Quilpué", "Viña del Mar", "2026-11-23", true);
        EventoRequestDto conIdBlanco = new EventoRequestDto(
                "   ", "Feria Sur", "Colegio C", "Concón", "Viña del Mar", "2026-11-24", false);

        when(eventoRepository.save(any(Evento.class))).thenAnswer(inv -> {
            Evento e = inv.getArgument(0);
            if (e.getId() == null) {
                e.setId("EVT-AUTO");
            }
            return e;
        });

        assertEquals("EVT-AUTO", eventoService.crear(conIdNulo).id());
        assertEquals("EVT-AUTO", eventoService.crear(conIdBlanco).id());
        verify(eventoRepository, never()).existsById(any());
    }

    @Test
    void crearLanzaConflict409CuandoIdYaExiste() {
        EventoRequestDto request = new EventoRequestDto(
                " EVT-DUP ", "Feria", "Liceo A", "Valparaíso", "Valparaíso", "2026-11-22", true);
        when(eventoRepository.existsById("EVT-DUP")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> eventoService.crear(request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void actualizarModificaEventoExistente() {
        Evento existente = sampleEvento("EVT-1", true);
        EventoRequestDto request = new EventoRequestDto(
                "EVT-1", "Feria Actualizada", "Liceo B", "Valparaíso", "Valparaíso", "2026-12-01", false);
        when(eventoRepository.findById("EVT-1")).thenReturn(Optional.of(existente));
        when(eventoRepository.save(any(Evento.class))).thenAnswer(inv -> inv.getArgument(0));

        EventoResponseDto response = eventoService.actualizar("EVT-1", request);

        assertEquals("Feria Actualizada", response.nombre());
        assertEquals("Liceo B", response.establecimiento());
        assertFalse(response.activo());
    }

    @Test
    void actualizarLanzaNotFound404CuandoNoExiste() {
        EventoRequestDto request = new EventoRequestDto(
                "EVT-404", "Feria", "Liceo", "Comuna", "Sede", "2026-12-01", true);
        when(eventoRepository.findById("EVT-404")).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> eventoService.actualizar("EVT-404", request));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void eliminarBorraEventoExistente() {
        Evento existente = sampleEvento("EVT-1", true);
        when(eventoRepository.findById("EVT-1")).thenReturn(Optional.of(existente));

        eventoService.eliminar("EVT-1");

        verify(eventoRepository).delete(existente);
    }

    @Test
    void eliminarLanzaNotFound404CuandoNoExiste() {
        when(eventoRepository.findById("EVT-404")).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> eventoService.eliminar("EVT-404"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verify(eventoRepository, never()).delete(any());
    }

    private Evento sampleEvento(String id, boolean activo) {
        return Evento.builder()
                .id(id)
                .nombre("Feria Duoc")
                .establecimiento("Liceo A")
                .comuna("Viña del Mar")
                .sedeResponsable("Viña del Mar")
                .fechaEvento("2026-11-22")
                .activo(activo)
                .createdAt(Instant.now())
                .build();
    }
}
