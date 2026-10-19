package com.therionlabs.captura_digital_v2.data.local

import androidx.room.TypeConverter
import com.therionlabs.captura_digital_v2.data.model.Genero
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.data.model.SyncStatus

/**
 * Conversores de tipo para Room SQLite.
 * Permiten almacenar los Enums de negocio como String en la base de datos
 * preservando el tipado fuerte y la integridad de los datos.
 */
class Converters {

    @TypeConverter
    fun fromGenero(value: Genero?): String? = value?.name

    @TypeConverter
    fun toGenero(value: String?): Genero? = value?.let {
        runCatching { Genero.valueOf(it) }.getOrDefault(Genero.NO_INDICA)
    }

    @TypeConverter
    fun fromSituacionAcademica(value: SituacionAcademica?): String? = value?.name

    @TypeConverter
    fun toSituacionAcademica(value: String?): SituacionAcademica? = value?.let {
        runCatching { SituacionAcademica.valueOf(it) }.getOrDefault(SituacionAcademica.OTRA)
    }

    @TypeConverter
    fun fromJornada(value: Jornada?): String? = value?.name

    @TypeConverter
    fun toJornada(value: String?): Jornada? = value?.let {
        runCatching { Jornada.valueOf(it) }.getOrDefault(Jornada.DIURNA)
    }

    @TypeConverter
    fun fromNivelInteres(value: NivelInteres?): String? = value?.name

    @TypeConverter
    fun toNivelInteres(value: String?): NivelInteres? = value?.let {
        runCatching { NivelInteres.valueOf(it) }.getOrDefault(NivelInteres.MEDIO)
    }

    @TypeConverter
    fun fromPerfilMotivacional(value: PerfilMotivacional?): String? = value?.name

    @TypeConverter
    fun toPerfilMotivacional(value: String?): PerfilMotivacional? = value?.let {
        runCatching { PerfilMotivacional.valueOf(it) }.getOrDefault(PerfilMotivacional.VOCACIONAL)
    }

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus?): String? = value?.name

    @TypeConverter
    fun toSyncStatus(value: String?): SyncStatus? = value?.let {
        runCatching { SyncStatus.valueOf(it) }.getOrDefault(SyncStatus.PENDING_SYNC)
    }
}
