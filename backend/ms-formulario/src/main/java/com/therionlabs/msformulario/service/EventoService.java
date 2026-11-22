package com.therionlabs.msformulario.service;

import com.therionlabs.msformulario.dto.EventoRequestDto;
import com.therionlabs.msformulario.dto.EventoResponseDto;
import com.therionlabs.msformulario.exception.ApiException;
import com.therionlabs.msformulario.mapper.ProspectoMapper;
import com.therionlabs.msformulario.model.Evento;
import com.therionlabs.msformulario.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final ProspectoMapper mapper;

    @Transactional(readOnly = true)
    public List<EventoResponseDto> listar(Boolean soloActivos) {
        List<Evento> eventos = Boolean.TRUE.equals(soloActivos)
                ? eventoRepository.findByActivoTrueOrderByCreatedAtDesc()
                : eventoRepository.findAll();
        return eventos.stream().map(mapper::toEventoResponseDto).toList();
    }

    @Transactional
    public EventoResponseDto crear(EventoRequestDto request) {
        if (request.id() != null && !request.id().isBlank() && eventoRepository.existsById(request.id().trim())) {
            throw ApiException.conflict("Ya existe un evento con el id: " + request.id().trim());
        }
        Evento evento = mapper.toEventoEntity(request);
        return mapper.toEventoResponseDto(eventoRepository.save(evento));
    }

    @Transactional
    public EventoResponseDto actualizar(String id, EventoRequestDto request) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Evento no encontrado: " + id));
        mapper.updateEventoEntity(evento, request);
        return mapper.toEventoResponseDto(eventoRepository.save(evento));
    }

    @Transactional
    public void eliminar(String id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Evento no encontrado: " + id));
        eventoRepository.delete(evento);
    }
}
