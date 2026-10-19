package com.therionlabs.captura_digital_v2.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.therionlabs.captura_digital_v2.data.model.Genero
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.data.model.SyncStatus

/**
 * Entidad de persistencia SQLite (Room) para almacenar prospectos capturados.
 * Mapea 1:1 los 22 campos del cupón físico de admisión Duoc UC.
 */
@Entity(
    tableName = "prospectos",
    indices = [
        Index(value = ["rut"], unique = true),
        Index(value = ["captadorId"]),
        Index(value = ["syncStatus"])
    ]
)
data class ProspectoEntity(
    @PrimaryKey
    val id: String,
    val captadorId: Int,
    val eventoId: String? = null,
    val fechaCaptura: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,

    // Consentimiento Legal (Ley N° 19.628 / 21.719)
    val consentimientoDatos: Boolean = true,

    // Datos Personales
    val rut: String,
    val nombres: String,
    val apellidos: String,
    val genero: Genero = Genero.NO_INDICA,
    val fechaNacimiento: String? = null,
    val comunaResidencia: String,
    val celular: String,
    val email: String,

    // Antecedentes Escolares
    val situacionAcademica: SituacionAcademica,
    val situacionAcademicaOtro: String? = null,
    val establecimientoEducacional: String,
    val comunaEstablecimiento: String,

    // Preferencias Institucionales
    val jornadaPreferencia: Jornada,
    val sedeInteres: String,

    // Calificación y Perfilamiento
    val nivelInteres: NivelInteres,
    val perfilMotivacional: PerfilMotivacional,

    // Matriz de Carreras (1 a 3 prioridades)
    val carreraPrioridad1: String,
    val carreraPrioridad2: String? = null,
    val carreraPrioridad3: String? = null
)
