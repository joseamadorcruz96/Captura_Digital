package com.therionlabs.captura_digital_v2.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad Room que registra una jornada o turno de captura asociado a un [Evento] y un captador.
 */
@Entity(
    tableName = "turnos_evento",
    indices = [
        Index(value = ["eventoId"]),
        Index(value = ["captadorId"]),
        Index(value = ["finEpoch"])
    ]
)
data class TurnoEvento(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val eventoId: String,
    val captadorId: Int,
    val inicioEpoch: Long,
    val finEpoch: Long? = null,
    val metaTurno: Int,
    val totalCapturados: Int = 0
) {
    val estaActivo: Boolean
        get() = finEpoch == null
}

/**
 * Resumen consolidado presentado al captador/administrador al finalizar un turno en terreno.
 */
data class ResumenCierreTurno(
    val turnoId: String,
    val eventoId: String,
    val eventoNombre: String,
    val inicioEpoch: Long,
    val finEpoch: Long,
    val duracionMs: Long,
    val metaTurno: Int,
    val totalCapturados: Int,
    val distribucionInteres: Map<NivelInteres, Int>,
    val pendientesSync: Int
)
