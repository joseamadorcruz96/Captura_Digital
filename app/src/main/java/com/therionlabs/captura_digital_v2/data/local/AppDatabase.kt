package com.therionlabs.captura_digital_v2.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.therionlabs.captura_digital_v2.data.local.dao.ProspectoDao
import com.therionlabs.captura_digital_v2.data.local.entity.ProspectoEntity
import com.therionlabs.captura_digital_v2.data.local.mapper.toEntity
import com.therionlabs.captura_digital_v2.data.model.Genero
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.data.model.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base de datos principal Room SQLite para Captura Digital v2.
 * Administra la tabla 'prospectos' e incluye callback de pre-población
 * para garantizar disponibilidad inmediata de datos demo en primera ejecución.
 */
@Database(
    entities = [ProspectoEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun prospectoDao(): ProspectoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "captura_digital.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.prospectoDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: ProspectoDao) {
            val initialList = listOf(
                Prospecto(
                    id = "demo-uuid-1",
                    captadorId = 2,
                    eventoId = "EVT-DUOC-2026",
                    rut = "20.123.456-5",
                    nombres = "Camila Andrea",
                    apellidos = "Valenzuela Soto",
                    genero = Genero.FEMENINO,
                    fechaNacimiento = "15/04/2008",
                    comunaResidencia = "Viña del Mar",
                    celular = "+56987654321",
                    email = "camila.valenzuela@gmail.com",
                    consentimientoDatos = true,
                    situacionAcademica = SituacionAcademica.CUARTO_MEDIO,
                    establecimientoEducacional = "Colegio Rubén Castro",
                    comunaEstablecimiento = "Viña del Mar",
                    jornadaPreferencia = Jornada.DIURNA,
                    sedeInteres = "Viña del Mar",
                    nivelInteres = NivelInteres.ALTO,
                    perfilMotivacional = PerfilMotivacional.VOCACIONAL,
                    carreraPrioridad1 = "Ingeniería en Informática",
                    carreraPrioridad2 = "Analista Programador",
                    syncStatus = SyncStatus.PENDING_SYNC
                ).toEntity(),
                Prospecto(
                    id = "demo-uuid-2",
                    captadorId = 2,
                    eventoId = "EVT-DUOC-2026",
                    rut = "11.111.111-1",
                    nombres = "Lucas Ignacio",
                    apellidos = "González Silva",
                    genero = Genero.MASCULINO,
                    fechaNacimiento = "22/09/2007",
                    comunaResidencia = "Valparaíso",
                    celular = "+56912345678",
                    email = "lucas.gonzalez@gmail.com",
                    consentimientoDatos = true,
                    situacionAcademica = SituacionAcademica.EGRESADO_EM,
                    establecimientoEducacional = "Liceo Eduardo de la Barra",
                    comunaEstablecimiento = "Valparaíso",
                    jornadaPreferencia = Jornada.DIURNA,
                    sedeInteres = "Valparaíso",
                    nivelInteres = NivelInteres.MEDIO,
                    perfilMotivacional = PerfilMotivacional.INVERSION,
                    carreraPrioridad1 = "Gastronomía Internacional",
                    carreraPrioridad2 = null,
                    syncStatus = SyncStatus.PENDING_SYNC
                ).toEntity()
            )
            dao.insertAll(initialList)
        }
    }
}
