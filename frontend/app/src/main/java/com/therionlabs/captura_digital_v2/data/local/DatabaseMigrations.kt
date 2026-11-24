package com.therionlabs.captura_digital_v2.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Registro centralizado y explícito de migraciones de [AppDatabase].
 * A partir de la versión 3, todo cambio de esquema debe definir su [Migration] explícita
 * en [ALL_MIGRATIONS] para preservar los datos locales offline.
 */
object DatabaseMigrations {

    /**
     * Ejemplo de migración base estructurada para futuras versiones (v4+ en Épica 1).
     * Se mantiene la lista ordenada de migraciones incrementalmente.
     */
    val ALL_MIGRATIONS: Array<Migration> = emptyArray()

    /**
     * Helper para validar o ejecutar alteraciones de esquema de forma segura en futuras migraciones.
     */
    internal fun hasColumn(db: SupportSQLiteDatabase, tableName: String, columnName: String): Boolean {
        db.query("PRAGMA table_info(`$tableName`)").use { cursor ->
            val nameIdx = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                if (nameIdx >= 0 && cursor.getString(nameIdx) == columnName) {
                    return true
                }
            }
        }
        return false
    }
}
