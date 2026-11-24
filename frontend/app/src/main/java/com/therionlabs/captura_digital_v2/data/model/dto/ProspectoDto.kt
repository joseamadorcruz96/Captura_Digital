package com.therionlabs.captura_digital_v2.data.model.dto

import com.therionlabs.captura_digital_v2.data.model.EstadoCompletitud
import com.therionlabs.captura_digital_v2.data.model.Evento
import com.therionlabs.captura_digital_v2.data.model.Genero
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.data.model.SyncStatus
import com.therionlabs.captura_digital_v2.data.model.TipoEvento
import com.therionlabs.captura_digital_v2.util.EventoDateUtils

/**
 * Data Transfer Objects (DTOs) para la comunicación con la API REST de Spring Boot.
 * Estos esquemas definen el contrato JSON exacto para el envío y consolidación remota.
 */

data class CarreraPreferenciaDto(
    val prioridad: Int,
    val nombreCarrera: String
)

data class ProspectoRequestDto(
    val clientUuid: String,
    val captadorId: Int,
    val eventoId: String?,
    val timestampEpoch: Long,
    val rut: String,
    val nombres: String,
    val apellidos: String,
    val genero: String,
    val fechaNacimiento: String?,
    val comunaResidencia: String,
    val celular: String,
    val email: String,
    val consentimientoDatos: Boolean,
    val situacionAcademica: String,
    val situacionAcademicaOtro: String?,
    val establecimientoEducacional: String,
    val comunaEstablecimiento: String,
    val jornadaPreferencia: String,
    val sedeInteres: String,
    val nivelInteres: String,
    val perfilMotivacional: String,
    val carreras: List<CarreraPreferenciaDto>,
    val estadoCompletitud: String? = null,
    val firmaDigitalBase64: String? = null,
    val firmaTimestampEpoch: Long? = null,
    val latitud: Double? = null,
    val longitud: Double? = null
)

data class ProspectoResponseDto(
    val clientUuid: String,
    val captadorId: Int,
    val eventoId: String?,
    val timestampEpoch: Long,
    val rut: String,
    val nombres: String,
    val apellidos: String,
    val genero: String,
    val fechaNacimiento: String?,
    val comunaResidencia: String,
    val celular: String,
    val email: String,
    val consentimientoDatos: Boolean,
    val situacionAcademica: String,
    val situacionAcademicaOtro: String?,
    val establecimientoEducacional: String,
    val comunaEstablecimiento: String,
    val jornadaPreferencia: String,
    val sedeInteres: String,
    val nivelInteres: String,
    val perfilMotivacional: String,
    val carreras: List<CarreraPreferenciaDto>,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val estadoCompletitud: String? = null,
    val firmaDigitalBase64: String? = null,
    val firmaTimestampEpoch: Long? = null,
    val latitud: Double? = null,
    val longitud: Double? = null
)

data class BatchSyncRequestDto(
    val captadorId: Int,
    val eventoId: String?,
    val prospectos: List<ProspectoRequestDto>
)

data class BatchSyncResponseDto(
    val totalProcesados: Int,
    val exitososUuids: List<String>,
    val fallidosUuids: List<String>,
    val mensaje: String,
    val erroresPorUuid: Map<String, String>? = null
)

data class EventoResponseDto(
    val id: String,
    val nombre: String,
    val establecimiento: String,
    val comuna: String,
    val sedeResponsable: String,
    val fechaEvento: String?,
    val activo: Boolean,
    val tipo: String? = null,
    val ubicacion: String? = null,
    val fechaInicioEpoch: Long? = null,
    val fechaFinEpoch: Long? = null,
    val metaProspectos: Int? = null,
    val archivado: Boolean = false
)

fun EventoResponseDto.toDomain(ahoraEpochMs: Long = System.currentTimeMillis()): Evento {
    val parsedTipo = tipo?.let { raw -> runCatching { TipoEvento.valueOf(raw) }.getOrNull() }
        ?: when {
            nombre.contains("colegio", ignoreCase = true) ||
                nombre.contains("liceo", ignoreCase = true) ||
                establecimiento.contains("colegio", ignoreCase = true) ||
                establecimiento.contains("liceo", ignoreCase = true) -> TipoEvento.COLEGIO
            nombre.contains("casa abierta", ignoreCase = true) -> TipoEvento.CASA_ABIERTA
            else -> TipoEvento.FERIA
        }

    val resolvedUbicacion = ubicacion?.takeIf { it.isNotBlank() } ?: run {
        when {
            establecimiento.isNotBlank() && comuna.isNotBlank() &&
                !establecimiento.contains(comuna, ignoreCase = true) -> "$establecimiento, $comuna"
            establecimiento.isNotBlank() -> establecimiento
            comuna.isNotBlank() -> comuna
            else -> sedeResponsable
        }
    }

    val inicio = fechaInicioEpoch
        ?: fechaEvento?.let { EventoDateUtils.parseDate(it, endOfDay = false) }
        ?: ahoraEpochMs
    val fin = fechaFinEpoch
        ?: fechaEvento?.let { EventoDateUtils.parseDate(it, endOfDay = true) }
        ?: (inicio + 8 * 60 * 60 * 1000L)

    return Evento(
        id = id,
        nombre = nombre,
        tipo = parsedTipo,
        sedeAsociada = sedeResponsable.ifBlank { comuna.ifBlank { "Por definir" } },
        ubicacion = resolvedUbicacion,
        fechaInicio = inicio,
        fechaFin = fin,
        metaProspectos = (metaProspectos ?: 30).coerceAtLeast(1),
        activo = activo,
        archivado = archivado
    )
}

fun Evento.toDto(): EventoResponseDto {
    val comunaExtraida = ubicacion.substringAfterLast(",", "").trim().ifBlank { sedeAsociada }
    return EventoResponseDto(
        id = id,
        nombre = nombre,
        establecimiento = ubicacion,
        comuna = comunaExtraida,
        sedeResponsable = sedeAsociada,
        fechaEvento = EventoDateUtils.formatIsoDate(fechaInicio),
        activo = activo,
        tipo = tipo.name,
        ubicacion = ubicacion,
        fechaInicioEpoch = fechaInicio,
        fechaFinEpoch = fechaFin,
        metaProspectos = metaProspectos,
        archivado = archivado
    )
}

data class DashboardResumenDto(
    val totalProspectos: Long,
    val conteoPorSede: Map<String, Long> = emptyMap(),
    val conteoPorNivelInteres: Map<String, Long> = emptyMap(),
    val conteoPorJornada: Map<String, Long> = emptyMap()
)

// Funciones de extensión (Mappers) entre Dominio y DTO

fun Prospecto.toDto(): ProspectoRequestDto {
    val carrerasList = mutableListOf<CarreraPreferenciaDto>()
    carrerasList.add(CarreraPreferenciaDto(1, carreraPrioridad1))
    carreraPrioridad2?.let { carrerasList.add(CarreraPreferenciaDto(2, it)) }
    carreraPrioridad3?.let { carrerasList.add(CarreraPreferenciaDto(3, it)) }

    return ProspectoRequestDto(
        clientUuid = id,
        captadorId = captadorId,
        eventoId = eventoId,
        timestampEpoch = fechaCaptura,
        rut = rut,
        nombres = nombres,
        apellidos = apellidos,
        genero = genero.name,
        fechaNacimiento = fechaNacimiento,
        comunaResidencia = comunaResidencia,
        celular = celular,
        email = email,
        consentimientoDatos = consentimientoDatos,
        situacionAcademica = situacionAcademica.name,
        situacionAcademicaOtro = situacionAcademicaOtro,
        establecimientoEducacional = establecimientoEducacional,
        comunaEstablecimiento = comunaEstablecimiento,
        jornadaPreferencia = jornadaPreferencia.name,
        sedeInteres = sedeInteres,
        nivelInteres = nivelInteres.name,
        perfilMotivacional = perfilMotivacional.name,
        carreras = carrerasList,
        estadoCompletitud = estadoCompletitud.name,
        firmaDigitalBase64 = firmaDigitalBase64,
        firmaTimestampEpoch = firmaTimestampEpoch,
        latitud = latitud,
        longitud = longitud
    )
}

fun ProspectoRequestDto.toDomain(syncStatus: SyncStatus = SyncStatus.SYNCED): Prospecto =
    mapFieldsToDomain(
        clientUuid = clientUuid,
        captadorId = captadorId,
        eventoId = eventoId,
        timestampEpoch = timestampEpoch,
        syncStatus = syncStatus,
        consentimientoDatos = consentimientoDatos,
        rut = rut,
        nombres = nombres,
        apellidos = apellidos,
        genero = genero,
        fechaNacimiento = fechaNacimiento,
        comunaResidencia = comunaResidencia,
        celular = celular,
        email = email,
        situacionAcademica = situacionAcademica,
        situacionAcademicaOtro = situacionAcademicaOtro,
        establecimientoEducacional = establecimientoEducacional,
        comunaEstablecimiento = comunaEstablecimiento,
        jornadaPreferencia = jornadaPreferencia,
        sedeInteres = sedeInteres,
        nivelInteres = nivelInteres,
        perfilMotivacional = perfilMotivacional,
        carreras = carreras,
        estadoCompletitud = estadoCompletitud,
        firmaDigitalBase64 = firmaDigitalBase64,
        firmaTimestampEpoch = firmaTimestampEpoch,
        latitud = latitud,
        longitud = longitud
    )

fun ProspectoResponseDto.toDomain(syncStatus: SyncStatus = SyncStatus.SYNCED): Prospecto =
    mapFieldsToDomain(
        clientUuid = clientUuid,
        captadorId = captadorId,
        eventoId = eventoId,
        timestampEpoch = timestampEpoch,
        syncStatus = syncStatus,
        consentimientoDatos = consentimientoDatos,
        rut = rut,
        nombres = nombres,
        apellidos = apellidos,
        genero = genero,
        fechaNacimiento = fechaNacimiento,
        comunaResidencia = comunaResidencia,
        celular = celular,
        email = email,
        situacionAcademica = situacionAcademica,
        situacionAcademicaOtro = situacionAcademicaOtro,
        establecimientoEducacional = establecimientoEducacional,
        comunaEstablecimiento = comunaEstablecimiento,
        jornadaPreferencia = jornadaPreferencia,
        sedeInteres = sedeInteres,
        nivelInteres = nivelInteres,
        perfilMotivacional = perfilMotivacional,
        carreras = carreras,
        estadoCompletitud = estadoCompletitud,
        firmaDigitalBase64 = firmaDigitalBase64,
        firmaTimestampEpoch = firmaTimestampEpoch,
        latitud = latitud,
        longitud = longitud
    )

private fun mapFieldsToDomain(
    clientUuid: String,
    captadorId: Int,
    eventoId: String?,
    timestampEpoch: Long,
    syncStatus: SyncStatus,
    consentimientoDatos: Boolean,
    rut: String,
    nombres: String,
    apellidos: String,
    genero: String,
    fechaNacimiento: String?,
    comunaResidencia: String,
    celular: String,
    email: String,
    situacionAcademica: String,
    situacionAcademicaOtro: String?,
    establecimientoEducacional: String,
    comunaEstablecimiento: String,
    jornadaPreferencia: String,
    sedeInteres: String,
    nivelInteres: String,
    perfilMotivacional: String,
    carreras: List<CarreraPreferenciaDto>,
    estadoCompletitud: String?,
    firmaDigitalBase64: String?,
    firmaTimestampEpoch: Long?,
    latitud: Double?,
    longitud: Double?
): Prospecto {
    val p1 = carreras.firstOrNull { it.prioridad == 1 }?.nombreCarrera ?: ""
    val p2 = carreras.firstOrNull { it.prioridad == 2 }?.nombreCarrera
    val p3 = carreras.firstOrNull { it.prioridad == 3 }?.nombreCarrera

    return Prospecto(
        id = clientUuid,
        captadorId = captadorId,
        eventoId = eventoId,
        fechaCaptura = timestampEpoch,
        syncStatus = syncStatus,
        consentimientoDatos = consentimientoDatos,
        rut = rut,
        nombres = nombres,
        apellidos = apellidos,
        genero = runCatching { Genero.valueOf(genero) }.getOrDefault(Genero.NO_INDICA),
        fechaNacimiento = fechaNacimiento,
        comunaResidencia = comunaResidencia,
        celular = celular,
        email = email,
        situacionAcademica = runCatching { SituacionAcademica.valueOf(situacionAcademica) }
            .getOrDefault(SituacionAcademica.CUARTO_MEDIO),
        situacionAcademicaOtro = situacionAcademicaOtro,
        establecimientoEducacional = establecimientoEducacional,
        comunaEstablecimiento = comunaEstablecimiento,
        jornadaPreferencia = runCatching { Jornada.valueOf(jornadaPreferencia) }
            .getOrDefault(Jornada.DIURNA),
        sedeInteres = sedeInteres,
        nivelInteres = runCatching { NivelInteres.valueOf(nivelInteres) }
            .getOrDefault(NivelInteres.ALTO),
        perfilMotivacional = runCatching { PerfilMotivacional.valueOf(perfilMotivacional) }
            .getOrDefault(PerfilMotivacional.INVERSION),
        carreraPrioridad1 = p1,
        carreraPrioridad2 = p2,
        carreraPrioridad3 = p3,
        estadoCompletitud = estadoCompletitud?.let { raw ->
            runCatching { EstadoCompletitud.valueOf(raw) }.getOrNull()
        } ?: EstadoCompletitud.COMPLETO,
        firmaDigitalBase64 = firmaDigitalBase64,
        firmaTimestampEpoch = firmaTimestampEpoch,
        latitud = latitud,
        longitud = longitud
    )
}
