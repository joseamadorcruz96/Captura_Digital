package com.therionlabs.captura_digital_v2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.therionlabs.captura_digital_v2.data.model.TurnoEvento
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para registrar el check-in y cierre de turnos por evento.
 */
@Dao
interface TurnoEventoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(turno: TurnoEvento): Long

    @Query("SELECT * FROM turnos_evento WHERE captadorId = :captadorId AND finEpoch IS NULL ORDER BY inicioEpoch DESC LIMIT 1")
    fun observeTurnoActivo(captadorId: Int): Flow<TurnoEvento?>

    @Query("SELECT * FROM turnos_evento WHERE captadorId = :captadorId AND finEpoch IS NULL ORDER BY inicioEpoch DESC LIMIT 1")
    suspend fun getTurnoActivo(captadorId: Int): TurnoEvento?

    @Query("SELECT * FROM turnos_evento WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TurnoEvento?

    @Query("SELECT * FROM turnos_evento WHERE eventoId = :eventoId ORDER BY inicioEpoch DESC")
    fun observeHistorialByEvento(eventoId: String): Flow<List<TurnoEvento>>
}
