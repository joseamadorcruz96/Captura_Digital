package com.therionlabs.msformulario.dto;

import java.time.Instant;
import java.util.List;

public record ProspectoResponseDto(
        String clientUuid,
        Integer captadorId,
        String eventoId,
        Long timestampEpoch,
        String rut,
        String nombres,
        String apellidos,
        String genero,
        String fechaNacimiento,
        String comunaResidencia,
        String celular,
        String email,
        boolean consentimientoDatos,
        String situacionAcademica,
        String situacionAcademicaOtro,
        String establecimientoEducacional,
        String comunaEstablecimiento,
        String jornadaPreferencia,
        String sedeInteres,
        String nivelInteres,
        String perfilMotivacional,
        List<CarreraPreferenciaDto> carreras,
        Instant createdAt,
        Instant updatedAt
) {}
