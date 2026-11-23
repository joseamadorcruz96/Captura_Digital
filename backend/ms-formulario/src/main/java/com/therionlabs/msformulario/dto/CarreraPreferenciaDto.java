package com.therionlabs.msformulario.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CarreraPreferenciaDto(
        @NotNull(message = "La prioridad es obligatoria")
        @Min(value = 1, message = "La prioridad mínima es 1")
        @Max(value = 3, message = "La prioridad máxima es 3")
        Integer prioridad,

        @NotBlank(message = "El nombre de la carrera es obligatorio")
        String nombreCarrera
) {}
