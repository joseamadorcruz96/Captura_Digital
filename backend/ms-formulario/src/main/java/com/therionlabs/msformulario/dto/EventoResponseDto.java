package com.therionlabs.msformulario.dto;

import java.time.Instant;

public record EventoResponseDto(
        String id,
        String nombre,
        String establecimiento,
        String comuna,
        String sedeResponsable,
        String fechaEvento,
        boolean activo,
        Instant createdAt
) {}
