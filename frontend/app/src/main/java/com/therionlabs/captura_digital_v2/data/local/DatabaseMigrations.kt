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
     * Migración v3 -> v4: incorpora las tablas 'eventos' y 'turnos_evento'
     * con sus índices y pre-pobla los eventos demo base si la tabla está vacía.
     */
    val MIGRATION_3_4: Migration = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `eventos` (
                    `id` TEXT NOT NULL,
                    `nombre` TEXT NOT NULL,
                    `tipo` TEXT NOT NULL,
                    `sedeAsociada` TEXT NOT NULL,
                    `ubicacion` TEXT NOT NULL,
                    `fechaInicio` INTEGER NOT NULL,
                    `fechaFin` INTEGER NOT NULL,
                    `metaProspectos` INTEGER NOT NULL,
                    `activo` INTEGER NOT NULL,
                    `archivado` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_eventos_activo` ON `eventos` (`activo`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_eventos_archivado` ON `eventos` (`archivado`)")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `turnos_evento` (
                    `id` TEXT NOT NULL,
                    `eventoId` TEXT NOT NULL,
                    `captadorId` INTEGER NOT NULL,
                    `inicioEpoch` INTEGER NOT NULL,
                    `finEpoch` INTEGER,
                    `metaTurno` INTEGER NOT NULL,
                    `totalCapturados` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_turnos_evento_eventoId` ON `turnos_evento` (`eventoId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_turnos_evento_captadorId` ON `turnos_evento` (`captadorId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_turnos_evento_finEpoch` ON `turnos_evento` (`finEpoch`)")

            DemoSeedGenerator.generarEventosDemo().forEach { evento ->
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO `eventos` (
                        `id`, `nombre`, `tipo`, `sedeAsociada`, `ubicacion`,
                        `fechaInicio`, `fechaFin`, `metaProspectos`, `activo`, `archivado`
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent(),
                    arrayOf<Any?>(
                        evento.id,
                        evento.nombre,
                        evento.tipo.name,
                        evento.sedeAsociada,
                        evento.ubicacion,
                        evento.fechaInicio,
                        evento.fechaFin,
                        evento.metaProspectos,
                        if (evento.activo) 1 else 0,
                        if (evento.archivado) 1 else 0
                    )
                )
            }
        }
    }

    val ALL_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_3_4)

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
