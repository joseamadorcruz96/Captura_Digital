package com.therionlabs.captura_digital_v2.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Tipos de evento / instancia de captura en terreno de Duoc UC.
 */
enum class TipoEvento(val displayName: String) {
    FERIA("Feria Vocacional"),
    COLEGIO("Visita a Colegio"),
    CASA_ABIERTA("Casa Abierta"),
    OTRO("Otro")
}

/**
 * Entidad Room y modelo de dominio que representa un Evento / Feria / Visita a Colegio.
 */
@Entity(
    tableName = "eventos",
    indices = [
        Index(value = ["activo"]),
        Index(value = ["archivado"])
    ]
)
data class Evento(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val tipo: TipoEvento = TipoEvento.FERIA,
    val sedeAsociada: String,
    val ubicacion: String,
    val fechaInicio: Long,
    val fechaFin: Long,
    val metaProspectos: Int,
    val activo: Boolean = true,
    val archivado: Boolean = false
) {
    val estaDisponibleParaCaptura: Boolean
        get() = activo && !archivado
}
