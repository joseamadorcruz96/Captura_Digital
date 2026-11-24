package com.therionlabs.captura_digital_v2

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteTransactionListener
import android.os.CancellationSignal
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteStatement
import com.therionlabs.captura_digital_v2.data.local.Converters
import com.therionlabs.captura_digital_v2.data.local.DatabaseMigrations
import com.therionlabs.captura_digital_v2.data.local.DemoSeedGenerator
import com.therionlabs.captura_digital_v2.data.model.TipoEvento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale

/**
 * Valida la migración explícita Room v3 -> v4 ([DatabaseMigrations.MIGRATION_3_4]),
 * los conversores de [TipoEvento] y la consistencia con el esquema exportado `4.json`.
 */
class DatabaseMigrationTest {

    private class RecordingSupportSqliteDb : SupportSQLiteDatabase {
        val executedSql = mutableListOf<String>()
        val executedArgs = mutableListOf<Array<out Any?>>()

        override fun execSQL(sql: String) {
            executedSql.add(sql)
        }

        override fun execSQL(sql: String, bindArgs: Array<out Any?>) {
            executedSql.add(sql)
            executedArgs.add(bindArgs)
        }

        override fun insert(table: String, conflictAlgorithm: Int, values: ContentValues): Long = 0L

        override fun compileStatement(sql: String): SupportSQLiteStatement = error("No usado")
        override fun beginTransaction() {}
        override fun beginTransactionNonExclusive() {}
        override fun beginTransactionWithListener(transactionListener: SQLiteTransactionListener) {}
        override fun beginTransactionWithListenerNonExclusive(transactionListener: SQLiteTransactionListener) {}
        override fun endTransaction() {}
        override fun setTransactionSuccessful() {}
        override fun inTransaction(): Boolean = false
        override val isDbLockedByCurrentThread: Boolean = false
        override fun yieldIfContendedSafely(): Boolean = false
        override fun yieldIfContendedSafely(sleepAfterYieldDelayMillis: Long): Boolean = false
        override var version: Int = 3
        override val maximumSize: Long = 0L
        override fun setMaximumSize(numBytes: Long): Long = numBytes
        override var pageSize: Long = 4096L
        override fun query(query: String): Cursor = error("No usado")
        override fun query(query: String, bindArgs: Array<out Any?>): Cursor = error("No usado")
        override fun query(query: SupportSQLiteQuery): Cursor = error("No usado")
        override fun query(query: SupportSQLiteQuery, cancellationSignal: CancellationSignal?): Cursor = error("No usado")
        override fun delete(table: String, whereClause: String?, whereArgs: Array<out Any?>?): Int = 0
        override fun update(table: String, conflictAlgorithm: Int, values: ContentValues, whereClause: String?, whereArgs: Array<out Any?>?): Int = 0
        override val isReadOnly: Boolean = false
        override val path: String = ":memory:"
        override val isWriteAheadLoggingEnabled: Boolean = false
        override fun enableWriteAheadLogging(): Boolean = false
        override fun disableWriteAheadLogging() {}
        override val isOpen: Boolean = true
        override val isDatabaseIntegrityOk: Boolean = true
        override fun setLocale(locale: Locale) {}
        override fun setMaxSqlCacheSize(cacheSize: Int) {}
        override fun setForeignKeyConstraintsEnabled(enabled: Boolean) {}
        override fun needUpgrade(newVersion: Int): Boolean = false
        override val attachedDbs: List<android.util.Pair<String, String>>? = null
        override fun close() {}
    }

    @Test
    fun migration3To4_creaTablasIndicesYPrePoblaEventosDemo() {
        val migration = DatabaseMigrations.ALL_MIGRATIONS.firstOrNull {
            it.startVersion == 3 && it.endVersion == 4
        }
        assertNotNull(migration)

        val db = RecordingSupportSqliteDb()
        migration!!.migrate(db)

        assertTrue(db.executedSql.any { it.contains("CREATE TABLE IF NOT EXISTS `eventos`") })
        assertTrue(db.executedSql.any { it.contains("CREATE TABLE IF NOT EXISTS `turnos_evento`") })
        assertTrue(db.executedSql.any { it.contains("index_eventos_activo") })
        assertTrue(db.executedSql.any { it.contains("index_eventos_archivado") })
        assertTrue(db.executedSql.any { it.contains("index_turnos_evento_eventoId") })
        assertTrue(db.executedSql.any { it.contains("index_turnos_evento_captadorId") })
        assertTrue(db.executedSql.any { it.contains("index_turnos_evento_finEpoch") })

        val expectedSeed = DemoSeedGenerator.generarEventosDemo()
        val inserts = db.executedSql.filter { it.contains("INSERT OR IGNORE INTO `eventos`") }
        assertEquals(expectedSeed.size, inserts.size)
        assertEquals(expectedSeed.size, db.executedArgs.size)
        assertTrue(db.executedArgs.any { it.firstOrNull() == DemoSeedGenerator.DEFAULT_EVENT_ID })
    }

    @Test
    fun schemaExportadoV4_contieneTablasEIndicesEsperados() {
        val candidates = listOf(
            File("schemas/com.therionlabs.captura_digital_v2.data.local.AppDatabase/4.json"),
            File("app/schemas/com.therionlabs.captura_digital_v2.data.local.AppDatabase/4.json"),
            File("frontend/app/schemas/com.therionlabs.captura_digital_v2.data.local.AppDatabase/4.json")
        )
        val schemaFile = candidates.firstOrNull { it.exists() }
        assertNotNull("Debe existir el esquema exportado 4.json de Room", schemaFile)

        val content = schemaFile!!.readText()
        assertTrue(content.contains("\"version\": 4"))
        assertTrue(content.contains("\"tableName\": \"prospectos\""))
        assertTrue(content.contains("\"tableName\": \"eventos\""))
        assertTrue(content.contains("\"tableName\": \"turnos_evento\""))
        assertTrue(content.contains("index_eventos_activo"))
        assertTrue(content.contains("index_eventos_archivado"))
        assertTrue(content.contains("index_turnos_evento_eventoId"))
        assertTrue(content.contains("index_turnos_evento_captadorId"))
        assertTrue(content.contains("index_turnos_evento_finEpoch"))
    }

    @Test
    fun converters_tipoEventoConvierteIdaYVueltaConFallbackSeguro() {
        val converters = Converters()
        TipoEvento.entries.forEach { tipo ->
            val raw = converters.fromTipoEvento(tipo)
            assertEquals(tipo, converters.toTipoEvento(raw))
        }
        assertEquals(TipoEvento.FERIA, converters.toTipoEvento("DESCONOCIDO"))
    }
}
