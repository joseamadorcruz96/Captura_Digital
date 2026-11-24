package com.therionlabs.captura_digital_v2.di

import android.content.Context
import com.therionlabs.captura_digital_v2.data.local.AppDatabase
import com.therionlabs.captura_digital_v2.data.local.DataStoreSessionStorage
import com.therionlabs.captura_digital_v2.data.local.LocalUserDataSource
import com.therionlabs.captura_digital_v2.data.local.SessionStorage
import com.therionlabs.captura_digital_v2.data.model.AppMode
import com.therionlabs.captura_digital_v2.data.remote.ConnectivityObserver
import com.therionlabs.captura_digital_v2.data.remote.IndicadoresApi
import com.therionlabs.captura_digital_v2.data.remote.NetworkMonitor
import com.therionlabs.captura_digital_v2.data.remote.RetrofitClient
import com.therionlabs.captura_digital_v2.data.repository.AuthRepository
import com.therionlabs.captura_digital_v2.data.repository.ProspectoRepository
import com.therionlabs.captura_digital_v2.data.repository.UsuarioRepository

/**
 * Contenedor de dependencias de la aplicación (Manual DI) para desacoplar ViewModels y Repositories.
 */
interface AppContainer {
    val sessionStorage: SessionStorage
    val localUserDataSource: LocalUserDataSource
    val authRepository: AuthRepository
    val usuarioRepository: UsuarioRepository
    val prospectoRepository: ProspectoRepository
    val connectivityObserver: ConnectivityObserver
    val indicadoresApi: IndicadoresApi
}

class DefaultAppContainer(
    context: Context,
    database: AppDatabase
) : AppContainer {

    override val sessionStorage: SessionStorage by lazy {
        DataStoreSessionStorage.create(
            context = context.applicationContext,
            defaultAppMode = AppMode.LOCAL_DEMO
        )
    }

    override val localUserDataSource: LocalUserDataSource by lazy {
        LocalUserDataSource()
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepository(
            api = RetrofitClient.authApi,
            localDataSource = localUserDataSource,
            sessionStorage = sessionStorage
        )
    }

    override val usuarioRepository: UsuarioRepository by lazy {
        UsuarioRepository(
            api = RetrofitClient.authApi,
            localDataSource = localUserDataSource,
            appModeProvider = { sessionStorage.currentAppMode }
        )
    }

    override val prospectoRepository: ProspectoRepository by lazy {
        ProspectoRepository(
            dao = database.prospectoDao(),
            api = RetrofitClient.prospectoApi,
            appModeProvider = { sessionStorage.currentAppMode }
        )
    }

    override val connectivityObserver: ConnectivityObserver by lazy {
        NetworkMonitor.observer()
    }

    override val indicadoresApi: IndicadoresApi by lazy {
        RetrofitClient.indicadoresApi
    }
}
