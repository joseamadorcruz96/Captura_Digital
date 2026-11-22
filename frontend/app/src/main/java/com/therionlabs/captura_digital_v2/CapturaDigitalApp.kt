package com.therionlabs.captura_digital_v2

import android.app.Application
import com.therionlabs.captura_digital_v2.data.local.AppDatabase
import com.therionlabs.captura_digital_v2.data.remote.NetworkMonitor

/**
 * Clase Application para Captura Digital v2.
 * Inicializa la base de datos Room SQLite y el monitor global de red a nivel de aplicación.
 */
class CapturaDigitalApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getDatabase(this)
        NetworkMonitor.init(this)
    }

    companion object {
        lateinit var instance: CapturaDigitalApp
            private set

        lateinit var database: AppDatabase
            private set

        fun isDatabaseInitialized(): Boolean = ::database.isInitialized
    }
}
