package com.therionlabs.captura_digital_v2

import android.app.Application
import com.therionlabs.captura_digital_v2.data.local.AppDatabase
import com.therionlabs.captura_digital_v2.data.remote.NetworkMonitor
import com.therionlabs.captura_digital_v2.di.AppContainer
import com.therionlabs.captura_digital_v2.di.DefaultAppContainer

/**
 * Clase Application para Captura Digital v2.
 * Inicializa la base de datos Room SQLite, el monitor global de red
 * y el contenedor de dependencias [AppContainer].
 */
class CapturaDigitalApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getDatabase(this)
        NetworkMonitor.init(this)
        container = DefaultAppContainer(this, database)
    }

    fun isContainerInitialized(): Boolean = ::container.isInitialized

    companion object {
        lateinit var instance: CapturaDigitalApp
            private set

        lateinit var database: AppDatabase
            private set

        fun isDatabaseInitialized(): Boolean = ::database.isInitialized

        fun containerOrNull(): AppContainer? =
            if (::instance.isInitialized && instance.isContainerInitialized()) instance.container else null
    }
}
