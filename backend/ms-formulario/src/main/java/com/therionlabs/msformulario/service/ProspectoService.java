package com.therionlabs.msformulario.service;

import com.therionlabs.msformulario.dto.BatchSyncRequestDto;
import com.therionlabs.msformulario.dto.BatchSyncResponseDto;
import com.therionlabs.msformulario.dto.DashboardResumenDto;
import com.therionlabs.msformulario.dto.ProspectoRequestDto;
import com.therionlabs.msformulario.dto.ProspectoResponseDto;
import com.therionlabs.msformulario.exception.ApiException;
import com.therionlabs.msformulario.mapper.ProspectoMapper;
import com.therionlabs.msformulario.model.Prospecto;
import com.therionlabs.msformulario.repository.ProspectoRepository;
import com.therionlabs.msformulario.util.RutValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProspectoService {

    private final ProspectoRepository prospectoRepository;
    private final ProspectoMapper mapper;
    private final ProspectoSyncItemProcessor syncItemProcessor;

    @Transactional(readOnly = true)
    public List<ProspectoResponseDto> listar(Integer captadorId, String eventoId, String rut, String sedeInteres) {
        String normalizedRut = (rut != null && !rut.isBlank()) ? RutValidator.normalize(rut) : null;
        String cleanEvento = (eventoId != null && !eventoId.isBlank()) ? eventoId.trim() : null;
        String cleanSede = (sedeInteres != null && !sedeInteres.isBlank()) ? sedeInteres.trim() : null;

        return prospectoRepository.findFiltered(captadorId, cleanEvento, normalizedRut, cleanSede).stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProspectoResponseDto obtener(String clientUuid) {
        return mapper.toResponseDto(buscarPorUuid(clientUuid));
    }

    @Transactional
    public ProspectoResponseDto crear(ProspectoRequestDto request) {
        validarRut(request.rut());
        String normalizedRut = RutValidator.normalize(request.rut());

        if (prospectoRepository.existsByRut(normalizedRut)) {
            throw ApiException.conflict("Ya existe un prospecto registrado con el RUT " + normalizedRut);
        }
        if (request.clientUuid() != null && !request.clientUuid().isBlank()
                && prospectoRepository.existsById(request.clientUuid().trim())) {
            throw ApiException.conflict("Ya existe un prospecto con clientUuid " + request.clientUuid().trim());
        }

        Prospecto saved = prospectoRepository.saveAndFlush(mapper.toNewEntity(request));
        return mapper.toResponseDto(saved);
    }

    @Transactional
    public ProspectoResponseDto actualizar(String clientUuid, ProspectoRequestDto request) {
        Prospecto existing = buscarPorUuid(clientUuid);
        validarRut(request.rut());
        String normalizedRut = RutValidator.normalize(request.rut());

        prospectoRepository.findByRut(normalizedRut).ifPresent(other -> {
            if (!other.getClientUuid().equals(existing.getClientUuid())) {
                throw ApiException.conflict("El RUT " + normalizedRut + " pertenece a otro prospecto");
            }
        });

        mapper.updateEntity(existing, request);
        return mapper.toResponseDto(prospectoRepository.saveAndFlush(existing));
    }

    @Transactional
    public void eliminar(String clientUuid) {
        Prospecto existing = buscarPorUuid(clientUuid);
        prospectoRepository.delete(existing);
    }

    /**
     * Orquesta la sincronización por lote SIN @Transactional global para permitir éxitos parciales.
     */
    public BatchSyncResponseDto sincronizarLote(BatchSyncRequestDto batch) {
        List<String> exitosos = new ArrayList<>();
        List<String> fallidos = new ArrayList<>();
        Map<String, String> erroresPorUuid = new LinkedHashMap<>();

        for (ProspectoRequestDto item : batch.prospectos()) {
            String itemUuid = (item != null && item.clientUuid() != null) ? item.clientUuid() : "SIN_UUID";
            try {
                String confirmedUuid = syncItemProcessor.procesarItem(item);
                exitosos.add(confirmedUuid);
            } catch (Exception ex) {
                log.warn("Fallo parcial sincronizando prospecto {}: {}", itemUuid, ex.getMessage());
                fallidos.add(itemUuid);
                erroresPorUuid.put(itemUuid, ex.getMessage() != null ? ex.getMessage() : "Error desconocido");
            }
        }

        String mensaje = fallidos.isEmpty()
                ? "Sincronización completada exitosamente."
                : "Sincronización parcial: " + exitosos.size() + " exitosos, " + fallidos.size() + " fallidos.";

        return new BatchSyncResponseDto(batch.prospectos().size(), exitosos, fallidos, mensaje, erroresPorUuid);
    }

    @Transactional(readOnly = true)
    public DashboardResumenDto obtenerResumen(String eventoId) {
        String cleanEvento = (eventoId != null && !eventoId.isBlank()) ? eventoId.trim() : null;
        long total = prospectoRepository.countByOptionalEventoId(cleanEvento);

        return new DashboardResumenDto(
                total,
                toCountMap(prospectoRepository.countGroupedBySede(cleanEvento)),
                toCountMap(prospectoRepository.countGroupedByNivelInteres(cleanEvento)),
                toCountMap(prospectoRepository.countGroupedByJornada(cleanEvento))
        );
    }

    private Prospecto buscarPorUuid(String clientUuid) {
        return prospectoRepository.findById(clientUuid)
                .orElseThrow(() -> ApiException.notFound("Prospecto no encontrado: " + clientUuid));
    }

    private void validarRut(String rut) {
        if (!RutValidator.isValid(rut)) {
            throw ApiException.badRequest("El RUT '" + rut + "' no supera la validación Módulo 11");
        }
    }

    private Map<String, Long> toCountMap(List<Object[]> rows) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String key = row[0] != null ? row[0].toString() : "SIN_DEFINIR";
            Long count = ((Number) row[1]).longValue();
            result.put(key, count);
        }
        return result;
    }
}
