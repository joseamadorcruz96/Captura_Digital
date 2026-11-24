package com.therionlabs.captura_digital_v2

import android.content.ContentResolver
import android.content.ContentValues
import android.database.CharArrayBuffer
import android.database.ContentObserver
import android.database.Cursor
import android.database.DataSetObserver
import android.database.sqlite.SQLiteTransactionListener
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteStatement
import com.therionlabs.captura_digital_v2.data.local.Converters
import com.therionlabs.captura_digital_v2.data.local.DatabaseMigrations
import com.therionlabs.captura_digital_v2.data.local.DemoSeedGenerator
import com.therionlabs.captura_digital_v2.data.model.EstadoCompletitud
import com.therionlabs.captura_digital_v2.data.model.FormularioBorrador
import com.therionlabs.captura_digital_v2.data.model.TipoEvento
import com.therionlabs.captura_digital_v2.data.repository.BorradorRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale

/**
 * Valida las migraciones explícitas Room v3 -> v4 y v4 -> v5 ([DatabaseMigrations]),
 * los conversores de enums, [BorradorRepository] y la consistencia con los esquemas exportados `4.json` y `5.json`.
 */
class DatabaseMigrationTest {

    private class TableInfoCursor(private val columns: List<String>) : Cursor {
        private var pos = -1
        private var closed = false
        override fun getCount(): Int = columns.size
        override fun getPosition(): Int = pos
        override fun move(offset: Int): Boolean = moveToPosition(pos + offset)
        override fun moveToPosition(position: Int): Boolean {
            if (position < -1 || position >= columns.size) return false
            pos = position
            return true
        }
        override fun moveToFirst(): Boolean = moveToPosition(0)
        override fun moveToLast(): Boolean = moveToPosition(columns.size - 1)
        override fun moveToNext(): Boolean = moveToPosition(pos + 1)
        override fun moveToPrevious(): Boolean = moveToPosition(pos - 1)
        override fun isFirst(): Boolean = pos == 0 && columns.isNotEmpty()
        override fun isLast(): Boolean = pos == columns.size - 1 && columns.isNotEmpty()
        override fun isBeforeFirst(): Boolean = pos < 0
        override fun isAfterLast(): Boolean = pos >= columns.size
        override fun getColumnIndex(columnName: String): Int = if (columnName == "name") 0 else -1
        override fun getColumnIndexOrThrow(columnName: String): Int =
            if (columnName == "name") 0 else throw IllegalArgumentException(columnName)
        override fun getColumnName(columnIndex: Int): String = "name"
        override fun getColumnNames(): Array<String> = arrayOf("name")
        override fun getColumnCount(): Int = 1
        override fun getBlob(columnIndex: Int): ByteArray = ByteArray(0)
        override fun getString(columnIndex: Int): String = columns[pos]
        override fun copyStringToBuffer(columnIndex: Int, buffer: CharArrayBuffer) {}
        override fun getShort(columnIndex: Int): Short = 0
        override fun getInt(columnIndex: Int): Int = 0
        override fun getLong(columnIndex: Int): Long = 0L
        override fun getFloat(columnIndex: Int): Float = 0f
        override fun getDouble(columnIndex: Int): Double = 0.0
        override fun getType(columnIndex: Int): Int = Cursor.FIELD_TYPE_STRING
        override fun isNull(columnIndex: Int): Boolean = false
        override fun deactivate() {}
        override fun requery(): Boolean = true
        override fun close() { closed = true }
        override fun isClosed(): Boolean = closed
        override fun registerContentObserver(observer: ContentObserver) {}
        override fun unregisterContentObserver(observer: ContentObserver) {}
        override fun registerDataSetObserver(observer: DataSetObserver) {}
        override fun unregisterDataSetObserver(observer: DataSetObserver) {}
        override fun setNotificationUri(cr: ContentResolver, uri: Uri) {}
        override fun getNotificationUri(): Uri? = null
        override fun getWantsAllOnMoveCalls(): Boolean = false
        override fun setExtras(extras: Bundle) {}
        override fun getExtras(): Bundle = Bundle.EMPTY
        override fun respond(extras: Bundle): Bundle = Bundle.EMPTY
    }

    private class RecordingSupportSqliteDb(
        private val existingProspectoColumns: List<String> = emptyList()
    ) : SupportSQLiteDatabase {
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
        override fun query(query: String): Cursor = TableInfoCursor(existingProspectoColumns)
        override fun query(query: String, bindArgs: Array<out Any?>): Cursor = TableInfoCursor(existingProspectoColumns)
        override fun query(query: SupportSQLiteQuery): Cursor = TableInfoCursor(existingProspectoColumns)
        override fun query(query: SupportSQLiteQuery, cancellationSignal: CancellationSignal?): Cursor =
            TableInfoCursor(existingProspectoColumns)
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

    private fun resolveSchemaFile(version: Int): File? {
        val candidates = listOf(
            File("schemas/com.therionlabs.captura_digital_v2.data.local.AppDatabase/$version.json"),
            File("app/schemas/com.therionlabs.captura_digital_v2.data.local.AppDatabase/$version.json"),
            File("frontend/app/schemas/com.therionlabs.captura_digital_v2.data.local.AppDatabase/$version.json")
        )
        return candidates.firstOrNull { it.exists() }
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
    fun migration4To5_agregaColumnasProspectosYCreaTablaBorradoresIdempotente() {
        val migration = DatabaseMigrations.ALL_MIGRATIONS.firstOrNull {
            it.startVersion == 4 && it.endVersion == 5
        }
        assertNotNull(migration)

        val freshDb = RecordingSupportSqliteDb(existingProspectoColumns = listOf("id", "rut"))
        migration!!.migrate(freshDb)

        assertTrue(freshDb.executedSql.any { it.contains("ADD COLUMN `estadoCompletitud` TEXT NOT NULL DEFAULT 'COMPLETO'") })
        assertTrue(freshDb.executedSql.any { it.contains("ADD COLUMN `firmaDigitalBase64` TEXT") })
        assertTrue(freshDb.executedSql.any { it.contains("ADD COLUMN `firmaTimestampEpoch` INTEGER") })
        assertTrue(freshDb.executedSql.any { it.contains("ADD COLUMN `latitud` REAL") })
        assertTrue(freshDb.executedSql.any { it.contains("ADD COLUMN `longitud` REAL") })
        assertTrue(freshDb.executedSql.any { it.contains("CREATE TABLE IF NOT EXISTS `formulario_borradores`") })
        assertTrue(freshDb.executedSql.any { it.contains("index_formulario_borradores_captadorId") })
        assertTrue(freshDb.executedSql.any { it.contains("index_formulario_borradores_updatedAtEpoch") })

        // Si las columnas ya existen, hasColumn evita duplicar ALTER TABLE
        val alreadyMigratedDb = RecordingSupportSqliteDb(
            existingProspectoColumns = listOf(
                "id", "estadoCompletitud", "firmaDigitalBase64", "firmaTimestampEpoch", "latitud", "longitud"
            )
        )
        migration.migrate(alreadyMigratedDb)
        assertFalse(alreadyMigratedDb.executedSql.any { it.contains("ALTER TABLE `prospectos`") })
        assertTrue(alreadyMigratedDb.executedSql.any { it.contains("CREATE TABLE IF NOT EXISTS `formulario_borradores`") })
    }

    @Test
    fun schemaExportadoV5_contieneTablasColumnasEIndicesEsperados() {
        val schemaFile = resolveSchemaFile(5)
        assertNotNull("Debe existir el esquema exportado 5.json de Room", schemaFile)

        val content = schemaFile!!.readText()
        assertTrue(content.contains("\"version\": 5"))
        assertTrue(content.contains("\"tableName\": \"prospectos\""))
        assertTrue(content.contains("\"columnName\": \"estadoCompletitud\""))
        assertTrue(content.contains("\"columnName\": \"firmaDigitalBase64\""))
        assertTrue(content.contains("\"columnName\": \"firmaTimestampEpoch\""))
        assertTrue(content.contains("\"columnName\": \"latitud\""))
        assertTrue(content.contains("\"columnName\": \"longitud\""))
        assertTrue(content.contains("\"tableName\": \"formulario_borradores\""))
        assertTrue(content.contains("index_formulario_borradores_captadorId"))
        assertTrue(content.contains("index_formulario_borradores_updatedAtEpoch"))
    }

    @Test
    fun converters_enumsConviertenIdaYVueltaConFallbackSeguro() {
        val converters = Converters()
        TipoEvento.entries.forEach { tipo ->
            val raw = converters.fromTipoEvento(tipo)
            assertEquals(tipo, converters.toTipoEvento(raw))
        }
        assertEquals(TipoEvento.FERIA, converters.toTipoEvento("DESCONOCIDO"))

        EstadoCompletitud.entries.forEach { estado ->
            val raw = converters.fromEstadoCompletitud(estado)
            assertEquals(estado, converters.toEstadoCompletitud(raw))
        }
        assertEquals(EstadoCompletitud.COMPLETO, converters.toEstadoCompletitud("DESCONOCIDO"))
    }

    @Test
    fun borradorRepository_exigeMinimoUnCampoYPermiteMultiplesBorradores() = runTest {
        val repo = BorradorRepository(dao = null)

        // Borrador vacío o solo checkbox sin datos es rechazado
        val vacio = FormularioBorrador(id = "d-vacio", captadorId = 2, consentimiento = true)
        assertFalse(repo.guardarBorrador(vacio))

        // Con al menos 1 campo (nombre) se guarda y soporta múltiples borradores
        val d1 = FormularioBorrador(id = "d-1", captadorId = 2, nombres = "Sofía", updatedAtEpoch = 1000L)
        val d2 = FormularioBorrador(id = "d-2", captadorId = 2, nombres = "Tomás", updatedAtEpoch = 2000L)
        assertTrue(repo.guardarBorrador(d1))
        assertTrue(repo.guardarBorrador(d2))

        val latest = repo.getLatestByCaptador(2)
        assertEquals("d-2", latest?.id)

        repo.eliminarBorrador("d-2")
        assertEquals("d-1", repo.getLatestByCaptador(2)?.id)
    }
}
