package com.therionlabs.msformulario.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Payload del lote Offline-First enviado desde Room hacia /api/v1/prospectos/sync.
 * No usa @Valid directo en la lista para permitir aislamiento ítem por ítem sin abortar todo el lote.
 */
public record BatchSyncRequestDto(
        @NotNull(message = "El captadorId es obligatorio")
        Integer captadorId,

        String eventoId,

        @NotEmpty(message = "La lista de prospectos no puede estar vacía")
        List<ProspectoRequestDto> prospectos
) {}
