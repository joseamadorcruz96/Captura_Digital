package com.therionlabs.captura_digital_v2

import android.app.Application
import com.therionlabs.captura_digital_v2.data.local.AppDatabase

/**
 * Clase Application para Captura Digital v2.
 * Inicializa la base de datos Room SQLite a nivel de aplicación para que
 * esté disponible globalmente sin fugas de memoria de Activities.
 */
class CapturaDigitalApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getDatabase(this)
    }

    companion object {
        lateinit var instance: CapturaDigitalApp
            private set

        lateinit var database: AppDatabase
            private set

        fun isDatabaseInitialized(): Boolean = ::database.isInitialized
    }
}
