package com.therionlabs.captura_digital_v2.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.therionlabs.captura_digital_v2.data.local.dao.ProspectoDao
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base de datos principal Room SQLite para Captura Digital v2.
 * Administra la tabla 'prospectos', exporta el esquema para migraciones verificables
 * e incluye callback de pre-población con datos demo realistas en primera ejecución.
 */
@Database(
    entities = [Prospecto::class],
    version = 3,
    exportSchema = true
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
                        populateInitialData(database.prospectoDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: ProspectoDao) {
            dao.insertAll(DemoSeedGenerator.generarProspectosDemo())
        }
    }
}
