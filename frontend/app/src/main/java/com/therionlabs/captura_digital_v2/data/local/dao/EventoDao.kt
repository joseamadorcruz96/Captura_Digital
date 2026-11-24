package com.therionlabs.captura_digital_v2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.therionlabs.captura_digital_v2.data.model.Evento
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para operaciones SQLite sobre la tabla "eventos".
 */
@Dao
interface EventoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(evento: Evento): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(eventos: List<Evento>): List<Long>

    @Query("SELECT * FROM eventos ORDER BY archivado ASC, activo DESC, fechaInicio ASC")
    fun observeAll(): Flow<List<Evento>>

    @Query("SELECT * FROM eventos WHERE activo = 1 AND archivado = 0 ORDER BY fechaInicio ASC")
    fun observeDisponibles(): Flow<List<Evento>>

    @Query("SELECT * FROM eventos ORDER BY archivado ASC, activo DESC, fechaInicio ASC")
    suspend fun getAllList(): List<Evento>

    @Query("SELECT * FROM eventos WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Evento?

    @Query("UPDATE eventos SET activo = :activo WHERE id = :id")
    suspend fun updateActivo(id: String, activo: Boolean): Int

    @Query("UPDATE eventos SET archivado = :archivado, activo = CASE WHEN :archivado = 1 THEN 0 ELSE activo END WHERE id = :id")
    suspend fun archivar(id: String, archivado: Boolean): Int

    @Query("DELETE FROM eventos")
    suspend fun deleteAll(): Int
}
