package com.therionlabs.msformulario.dto;

import jakarta.validation.constraints.NotBlank;

public record EventoRequestDto(
        String id,

        @NotBlank(message = "El nombre del evento es obligatorio")
        String nombre,

        @NotBlank(message = "El establecimiento es obligatorio")
        String establecimiento,

        @NotBlank(message = "La comuna es obligatoria")
        String comuna,

        @NotBlank(message = "La sede responsable es obligatoria")
        String sedeResponsable,

        String fechaEvento,
        Boolean activo
) {}
