package com.therionlabs.captura_digital_v2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para operaciones SQLite sobre la tabla "prospectos".
 * Incluye queries reactivos (Flow) y suspend functions para arquitectura Offline-First.
 */
@Dao
interface ProspectoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(prospecto: Prospecto): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prospectos: List<Prospecto>): List<Long>

    @Query("SELECT * FROM prospectos ORDER BY fechaCaptura DESC")
    fun getAllFlow(): Flow<List<Prospecto>>

    @Query("SELECT * FROM prospectos ORDER BY fechaCaptura DESC")
    suspend fun getAllList(): List<Prospecto>

    @Query("SELECT * FROM prospectos WHERE captadorId = :captadorId ORDER BY fechaCaptura DESC")
    fun getByCaptadorFlow(captadorId: Int): Flow<List<Prospecto>>

    @Query("SELECT * FROM prospectos WHERE captadorId = :captadorId ORDER BY fechaCaptura DESC")
    suspend fun getByCaptadorList(captadorId: Int): List<Prospecto>

    @Query("SELECT * FROM prospectos WHERE rut = :rut LIMIT 1")
    suspend fun getByRut(rut: String): Prospecto?

    @Query("SELECT * FROM prospectos WHERE syncStatus != :syncedStatus ORDER BY fechaCaptura ASC")
    suspend fun getPendingSyncList(syncedStatus: SyncStatus = SyncStatus.SYNCED): List<Prospecto>

    @Query("UPDATE prospectos SET syncStatus = :status WHERE id IN (:ids)")
    suspend fun updateSyncStatusByIds(ids: List<String>, status: SyncStatus): Int

    @Query("SELECT COUNT(*) FROM prospectos WHERE syncStatus = :status")
    suspend fun countBySyncStatus(status: SyncStatus = SyncStatus.PENDING_SYNC): Int

    @Query("SELECT COUNT(*) FROM prospectos WHERE syncStatus = :status")
    fun countBySyncStatusFlow(status: SyncStatus = SyncStatus.PENDING_SYNC): Flow<Int>

    @Query("DELETE FROM prospectos WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM prospectos")
    suspend fun deleteAll(): Int
}
