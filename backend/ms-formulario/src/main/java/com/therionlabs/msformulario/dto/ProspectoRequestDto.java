package com.therionlabs.msformulario.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Contrato de entrada alineado 1:1 con ProspectoRequestDto de la app Android (ProspectoDto.kt).
 */
public record ProspectoRequestDto(
        String clientUuid,

        @NotNull(message = "El identificador del captador es obligatorio")
        Integer captadorId,

        String eventoId,
        Long timestampEpoch,

        @NotBlank(message = "El RUT es obligatorio")
        String rut,

        @NotBlank(message = "Los nombres son obligatorios")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        String apellidos,

        @NotBlank(message = "El género es obligatorio")
        String genero,

        String fechaNacimiento,

        @NotBlank(message = "La comuna de residencia es obligatoria")
        String comunaResidencia,

        String celular,

        @Email(message = "Formato de correo electrónico inválido")
        String email,

        @AssertTrue(message = "Debe autorizar el consentimiento informado de datos")
        boolean consentimientoDatos,

        @NotBlank(message = "La situación académica es obligatoria")
        String situacionAcademica,

        String situacionAcademicaOtro,

        @NotBlank(message = "El establecimiento educacional es obligatorio")
        String establecimientoEducacional,

        @NotBlank(message = "La comuna del establecimiento es obligatoria")
        String comunaEstablecimiento,

        @NotBlank(message = "La jornada de preferencia es obligatoria")
        String jornadaPreferencia,

        @NotBlank(message = "La sede de interés es obligatoria")
        String sedeInteres,

        @NotBlank(message = "El nivel de interés es obligatorio")
        String nivelInteres,

        @NotBlank(message = "El perfil motivacional es obligatorio")
        String perfilMotivacional,

        @NotEmpty(message = "Debe seleccionar al menos una carrera de interés")
        @Size(max = 3, message = "Se permiten como máximo 3 prioridades de carrera")
        List<@Valid CarreraPreferenciaDto> carreras
) {
    /**
     * Alineado con FormularioValidator.validarContacto de Android: exige celular o correo.
     */
    @JsonIgnore
    @AssertTrue(message = "Debe ingresar al menos un canal de contacto (celular o correo electrónico)")
    public boolean isContactoValido() {
        boolean tieneCelular = celular != null && !celular.isBlank();
        boolean tieneEmail = email != null && !email.isBlank();
        return tieneCelular || tieneEmail;
    }
}
