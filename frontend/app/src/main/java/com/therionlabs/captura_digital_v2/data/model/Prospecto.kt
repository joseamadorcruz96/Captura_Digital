package com.therionlabs.captura_digital_v2.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Modelo de Dominio y Entidad Room que representa a un Prospecto / Cupón de Admisión Duoc UC.
 * Única fuente de la verdad compartida entre dominio y persistencia SQLite.
 */
@Entity(
    tableName = "prospectos",
    indices = [
        Index(value = ["rut"], unique = true),
        Index(value = ["captadorId"]),
        Index(value = ["syncStatus"])
    ]
)
data class Prospecto(
    // Identificadores y Control Operativo Offline
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val captadorId: Int,
    val eventoId: String? = null,
    val fechaCaptura: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,

    // Consentimiento Legal (Ley N° 19.628 / 21.719)
    val consentimientoDatos: Boolean = true,
    // Verificado 13-10-2026
    // Datos Personales
    val rut: String,
    val nombres: String,
    val apellidos: String,
    val genero: Genero = Genero.NO_INDICA,
    val fechaNacimiento: String? = null,
    val comunaResidencia: String,
    val celular: String,
    val email: String,

    // Antecedentes Escolares - verificado 13-10-2026
    val situacionAcademica: SituacionAcademica,
    val situacionAcademicaOtro: String? = null,
    val establecimientoEducacional: String,
    val comunaEstablecimiento: String,

    // Preferencias Institucionales - verificado 13-10-2026
    val jornadaPreferencia: Jornada,
    val sedeInteres: String,

    // Calificación y Perfilamiento - verificado 13-10-2026
    val nivelInteres: NivelInteres,
    val perfilMotivacional: PerfilMotivacional,

    // Matriz de Carreras (1 a 3 prioridades)
    val carreraPrioridad1: String,
    val carreraPrioridad2: String? = null,
    val carreraPrioridad3: String? = null
) {
    val nombreCompleto: String
        get() = "$nombres $apellidos".trim()

    val cantidadCarrerasElegidas: Int
        get() = listOfNotNull(carreraPrioridad1, carreraPrioridad2, carreraPrioridad3).size
}
