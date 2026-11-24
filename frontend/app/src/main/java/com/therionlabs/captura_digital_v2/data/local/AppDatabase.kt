package com.therionlabs.captura_digital_v2.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.therionlabs.captura_digital_v2.data.local.dao.EventoDao
import com.therionlabs.captura_digital_v2.data.local.dao.ProspectoDao
import com.therionlabs.captura_digital_v2.data.local.dao.TurnoEventoDao
import com.therionlabs.captura_digital_v2.data.model.Evento
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.TurnoEvento
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base de datos principal Room SQLite para Captura Digital v2.
 * Administra 'prospectos', 'eventos' y 'turnos_evento', exporta el esquema para migraciones
 * verificables e incluye pre-población inicial de datos demo realistas.
 */
@Database(
    entities = [Prospecto::class, Evento::class, TurnoEvento::class],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun prospectoDao(): ProspectoDao
    abstract fun eventoDao(): EventoDao
    abstract fun turnoEventoDao(): TurnoEventoDao

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
                    .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
                    // Solo versiones heredadas previas a v3 pueden reconstruirse si no tenían historial;
                    // desde v3+ toda evolución requiere Migration explícita en DatabaseMigrations.
                    .fallbackToDestructiveMigrationFrom(dropAllTables = true, 1, 2)
                    .addCallback(DatabaseCallback())
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
                        populateInitialData(database.prospectoDao(), database.eventoDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(
            prospectoDao: ProspectoDao,
            eventoDao: EventoDao? = null
        ) {
            prospectoDao.insertAll(DemoSeedGenerator.generarProspectosDemo())
            eventoDao?.insertAll(DemoSeedGenerator.generarEventosDemo())
        }
    }
}
