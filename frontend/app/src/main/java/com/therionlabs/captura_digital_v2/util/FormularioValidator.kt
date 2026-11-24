package com.therionlabs.captura_digital_v2.util

import com.therionlabs.captura_digital_v2.data.model.FormularioBorrador
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional

/**
 * Resultado inmutable de una validación de campo o regla de negocio.
 */
sealed class Resultado {
    data object Validado : Resultado()
    data class Invalido(val causa: String) : Resultado()

    val isValid: Boolean
        get() = this is Validado

    fun errorOrNull(): String? = (this as? Invalido)?.causa
}

/**
 * Resultado combinado para los canales de contacto (al menos uno requerido).
 */
data class ContactoValidation(
    val emailResultado: Resultado,
    val celularResultado: Resultado
) {
    val isValid: Boolean
        get() = emailResultado.isValid && celularResultado.isValid
}

/**
 * Capa de validación pura desacoplada del ViewModel.
 * Centraliza todas las reglas de negocio del Cupón de Admisión Duoc UC.
 */
object FormularioValidator {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validarConsentimiento(aceptado: Boolean): Resultado {
        return if (aceptado) {
            Resultado.Validado
        } else {
            Resultado.Invalido("Debe aceptar el consentimiento para continuar")
        }
    }

    /**
     * Valida la firma digital táctil capturada sobre el Canvas.
     */
    fun validarFirmaDigital(firmaBase64: String?): Resultado {
        return if (!firmaBase64.isNullOrBlank()) {
            Resultado.Validado
        } else {
            Resultado.Invalido("Debe registrar la firma digital del postulante")
        }
    }

    /**
     * Regla combinada de consentimiento + firma digital.
     * Cuando [exigirFirma] es `false` (por defecto en flujos que no dibujan en Canvas),
     * solo exige aceptación del consentimiento; cuando es `true`, valida ambos.
     */
    fun validarConsentimientoConFirma(
        aceptado: Boolean,
        firmaBase64: String?,
        exigirFirma: Boolean = false
    ): Resultado {
        val consentRes = validarConsentimiento(aceptado)
        if (!consentRes.isValid) return consentRes
        return if (exigirFirma) validarFirmaDigital(firmaBase64) else Resultado.Validado
    }

    /**
     * Regla mínima para guardar un borrador local (no sincronizable):
     * exige al menos 1 campo de datos completado (ej. nombre o RUT).
     */
    fun validarBorradorMinimo(borrador: FormularioBorrador): Resultado {
        return if (borrador.hasMinimumDraftData()) {
            Resultado.Validado
        } else {
            Resultado.Invalido("Ingresa al menos un dato (ej: nombre o RUT) para guardar el borrador")
        }
    }

    /**
     * Valida los campos esenciales requeridos por el Modo Captura Express (Fast-Track).
     */
    fun validarExpress(
        consentimiento: Boolean,
        firmaBase64: String?,
        exigirFirma: Boolean = false,
        rut: String,
        nombres: String,
        apellidos: String,
        email: String,
        celular: String,
        sedeInteres: String?,
        carreraPrioridad1: String
    ): List<String> {
        val errores = mutableListOf<String>()
        validarConsentimientoConFirma(consentimiento, firmaBase64, exigirFirma).errorOrNull()?.let(errores::add)
        validarRut(rut).errorOrNull()?.let(errores::add)
        validarNombres(nombres).errorOrNull()?.let(errores::add)
        validarApellidos(apellidos).errorOrNull()?.let(errores::add)
        val contacto = validarContacto(email, celular)
        if (!contacto.isValid) {
            contacto.emailResultado.errorOrNull()?.let(errores::add)
                ?: contacto.celularResultado.errorOrNull()?.let(errores::add)
        }
        validarSede(sedeInteres).errorOrNull()?.let(errores::add)
        validarCarreraPrioridad1(carreraPrioridad1).errorOrNull()?.let(errores::add)
        return errores.distinct()
    }

    fun validarRut(rut: String): Resultado {
        return if (rut.isNotBlank() && RutUtils.isValidRut(rut)) {
            Resultado.Validado
        } else {
            Resultado.Invalido("Ingrese un RUT chileno válido (ej: 12.345.678-5)")
        }
    }

    fun validarNombres(nombres: String): Resultado =
        validarTextoPersona(nombres, nombreCampo = "El nombre")

    fun validarApellidos(apellidos: String): Resultado =
        validarTextoPersona(apellidos, nombreCampo = "Los apellidos")

    private fun validarTextoPersona(valor: String, nombreCampo: String): Resultado {
        if (valor.isBlank()) {
            return Resultado.Invalido("$nombreCampo es obligatorio")
        }
        val length = valor.trim().length
        if (length < 2) {
            return Resultado.Invalido("$nombreCampo debe contener al menos 2 caracteres")
        }
        if (length > 50) {
            return Resultado.Invalido("$nombreCampo no debe superar los 50 caracteres")
        }
        return Resultado.Validado
    }

    fun validarComunaResidencia(comuna: String): Resultado {
        if (comuna.isBlank()) {
            return Resultado.Invalido("La comuna de residencia es obligatoria")
        }
        if (comuna.trim().length < 2) {
            return Resultado.Invalido("Ingrese una comuna de residencia válida")
        }
        return Resultado.Validado
    }

    fun validarEmail(email: String): Resultado {
        val trimmed = email.trim()
        if (trimmed.isBlank()) {
            return Resultado.Validado
        }
        return if (EMAIL_REGEX.matches(trimmed)) {
            Resultado.Validado
        } else {
            Resultado.Invalido("Formato de correo electrónico inválido")
        }
    }

    fun validarCelular(celular: String): Resultado {
        if (celular.isBlank()) {
            return Resultado.Validado
        }
        return if (celular.length in 8..9 && celular.all { it.isDigit() }) {
            Resultado.Validado
        } else {
            Resultado.Invalido("Ingrese entre 8 y 9 dígitos")
        }
    }

    fun validarContacto(email: String, celular: String): ContactoValidation {
        if (email.isBlank() && celular.isBlank()) {
            val error = Resultado.Invalido("Debe ingresar al menos un canal de contacto (Email o Celular)")
            return ContactoValidation(emailResultado = error, celularResultado = error)
        }
        return ContactoValidation(
            emailResultado = validarEmail(email),
            celularResultado = validarCelular(celular)
        )
    }

    fun validarEstablecimiento(establecimiento: String): Resultado {
        if (establecimiento.isBlank()) {
            return Resultado.Invalido("El colegio o liceo de origen es obligatorio")
        }
        if (establecimiento.trim().length < 2) {
            return Resultado.Invalido("Ingrese un establecimiento educacional válido")
        }
        return Resultado.Validado
    }

    fun validarSede(sedeInteres: String?): Resultado {
        return if (!sedeInteres.isNullOrBlank()) {
            Resultado.Validado
        } else {
            Resultado.Invalido("Debe seleccionar una sede de interés")
        }
    }

    fun validarNivelInteres(nivelInteres: NivelInteres?): Resultado {
        return if (nivelInteres != null) {
            Resultado.Validado
        } else {
            Resultado.Invalido("Seleccione el nivel de intención de matrícula")
        }
    }

    fun validarPerfilMotivacional(perfilMotivacional: PerfilMotivacional?): Resultado {
        return if (perfilMotivacional != null) {
            Resultado.Validado
        } else {
            Resultado.Invalido("Seleccione la motivación principal del postulante")
        }
    }

    fun validarCarreraPrioridad1(carrera: String): Resultado {
        return if (carrera.isNotBlank()) {
            Resultado.Validado
        } else {
            Resultado.Invalido("Seleccione al menos una carrera como primera preferencia")
        }
    }
}
