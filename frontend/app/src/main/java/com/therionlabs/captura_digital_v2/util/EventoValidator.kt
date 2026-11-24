package com.therionlabs.captura_digital_v2.util

/**
 * Validaciones de negocio para la creación y edición de Eventos / Ferias.
 */
object EventoValidator {

    fun validarNombre(nombre: String): Resultado {
        val trimmed = nombre.trim()
        return when {
            trimmed.isEmpty() -> Resultado.Invalido("El nombre del evento es obligatorio")
            trimmed.length < 3 -> Resultado.Invalido("El nombre debe tener al menos 3 caracteres")
            trimmed.length > 100 -> Resultado.Invalido("El nombre no puede superar los 100 caracteres")
            else -> Resultado.Validado
        }
    }

    fun validarSede(sede: String): Resultado {
        return if (sede.trim().isEmpty()) {
            Resultado.Invalido("Seleccione la sede asociada")
        } else {
            Resultado.Validado
        }
    }

    fun validarUbicacion(ubicacion: String): Resultado {
        val trimmed = ubicacion.trim()
        return when {
            trimmed.isEmpty() -> Resultado.Invalido("La ubicación o establecimiento es obligatorio")
            trimmed.length < 3 -> Resultado.Invalido("Ingrese una ubicación más descriptiva")
            else -> Resultado.Validado
        }
    }

    fun validarMeta(metaRaw: String): Resultado {
        val meta = metaRaw.trim().toIntOrNull()
        return when {
            metaRaw.isBlank() -> Resultado.Invalido("La meta de prospectos es obligatoria")
            meta == null -> Resultado.Invalido("Ingrese un número entero válido")
            meta <= 0 -> Resultado.Invalido("La meta debe ser mayor a 0")
            meta > 50_000 -> Resultado.Invalido("La meta no puede superar 50.000")
            else -> Resultado.Validado
        }
    }

    fun validarFecha(fechaRaw: String, etiqueta: String): Resultado {
        if (fechaRaw.isBlank()) {
            return Resultado.Invalido("La $etiqueta es obligatoria (dd/MM/yyyy)")
        }
        return if (EventoDateUtils.parseDate(fechaRaw) != null) {
            Resultado.Validado
        } else {
            Resultado.Invalido("Formato inválido en $etiqueta (use dd/MM/yyyy)")
        }
    }

    fun validarRangoFechas(fechaInicioRaw: String, fechaFinRaw: String): Resultado {
        val inicio = EventoDateUtils.parseDate(fechaInicioRaw, endOfDay = false)
            ?: return Resultado.Invalido("Fecha de inicio inválida")
        val fin = EventoDateUtils.parseDate(fechaFinRaw, endOfDay = true)
            ?: return Resultado.Invalido("Fecha de término inválida")

        return if (fin < inicio) {
            Resultado.Invalido("La fecha de término no puede ser anterior a la de inicio")
        } else {
            Resultado.Validado
        }
    }
}
