package com.therionlabs.msformulario.service;

import com.therionlabs.msformulario.dto.ProspectoRequestDto;
import com.therionlabs.msformulario.exception.ApiException;
import com.therionlabs.msformulario.mapper.ProspectoMapper;
import com.therionlabs.msformulario.model.Prospecto;
import com.therionlabs.msformulario.repository.ProspectoRepository;
import com.therionlabs.msformulario.util.RutValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Bean separado para ejecutar cada ítem del Batch Sync en una transacción independiente (REQUIRES_NEW)
 * evitando el bypass del proxy AOP de Spring por self-invocation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProspectoSyncItemProcessor {

    private final ProspectoRepository prospectoRepository;
    private final ProspectoMapper mapper;
    private final Validator validator;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String procesarItem(ProspectoRequestDto item) {
        if (item == null) {
            throw ApiException.badRequest("Ítem nulo en el lote");
        }

        Set<ConstraintViolation<ProspectoRequestDto>> violations = validator.validate(item);
        if (!violations.isEmpty()) {
            String details = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            throw ApiException.badRequest("Validación fallida: " + details);
        }

        if (!RutValidator.isValid(item.rut())) {
            throw ApiException.badRequest("RUT inválido (Módulo 11): " + item.rut());
        }

        String normalizedRut = RutValidator.normalize(item.rut());
        Prospecto entity = prospectoRepository.findByRut(normalizedRut)
                .map(existing -> {
                    mapper.updateEntity(existing, item);
                    return existing;
                })
                .orElseGet(() -> mapper.toNewEntity(item));

        Prospecto saved = prospectoRepository.saveAndFlush(entity);
        log.debug("Prospecto sincronizado (RUT={}, uuidBD={}, uuidCliente={})",
                normalizedRut, saved.getClientUuid(), item.clientUuid());

        // Retorna el UUID enviado por el dispositivo solicitante para destrabar su estado PENDING_SYNC en Room
        return (item.clientUuid() != null && !item.clientUuid().isBlank())
                ? item.clientUuid().trim()
                : saved.getClientUuid();
    }
}
