package com.therionlabs.captura_digital_v2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.therionlabs.captura_digital_v2.data.model.FormularioBorrador
import kotlinx.coroutines.flow.Flow

/**
 * DAO para borradores locales de formularios de captura en progreso.
 */
@Dao
interface BorradorDao {

    @Query("SELECT * FROM formulario_borradores WHERE captadorId = :captadorId ORDER BY updatedAtEpoch DESC")
    fun observeByCaptador(captadorId: Int): Flow<List<FormularioBorrador>>

    @Query("SELECT * FROM formulario_borradores ORDER BY updatedAtEpoch DESC")
    fun observeAll(): Flow<List<FormularioBorrador>>

    @Query("SELECT * FROM formulario_borradores WHERE captadorId = :captadorId ORDER BY updatedAtEpoch DESC")
    suspend fun getByCaptadorList(captadorId: Int): List<FormularioBorrador>

    @Query("SELECT * FROM formulario_borradores ORDER BY updatedAtEpoch DESC")
    suspend fun getAllList(): List<FormularioBorrador>

    @Query("SELECT * FROM formulario_borradores WHERE captadorId = :captadorId ORDER BY updatedAtEpoch DESC LIMIT 1")
    suspend fun getLatestByCaptador(captadorId: Int): FormularioBorrador?

    @Query("SELECT * FROM formulario_borradores WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): FormularioBorrador?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(borrador: FormularioBorrador): Long

    @Query("DELETE FROM formulario_borradores WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM formulario_borradores WHERE captadorId = :captadorId")
    suspend fun deleteByCaptador(captadorId: Int): Int

    @Query("DELETE FROM formulario_borradores")
    suspend fun deleteAll(): Int
}
