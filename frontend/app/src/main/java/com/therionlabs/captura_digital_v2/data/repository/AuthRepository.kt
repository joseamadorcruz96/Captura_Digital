package com.therionlabs.captura_digital_v2.data.repository

import com.therionlabs.captura_digital_v2.data.local.InMemorySessionStorage
import com.therionlabs.captura_digital_v2.data.local.LocalUserDataSource
import com.therionlabs.captura_digital_v2.data.local.PersistedSession
import com.therionlabs.captura_digital_v2.data.local.SessionStorage
import com.therionlabs.captura_digital_v2.data.model.AppMode
import com.therionlabs.captura_digital_v2.data.model.User
import com.therionlabs.captura_digital_v2.data.model.UserRole
import com.therionlabs.captura_digital_v2.data.remote.AuthApi
import com.therionlabs.captura_digital_v2.data.remote.LoginBody
import com.therionlabs.captura_digital_v2.data.remote.RecuperarBody
import com.therionlabs.captura_digital_v2.data.remote.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Repositorio de autenticación desacoplado en DataSources local/remoto y sesión persistente.
 * Soporta operación 100% local en [AppMode.LOCAL_DEMO] o vía JWT/Gateway en [AppMode.REMOTE_GATEWAY].
 */
class AuthRepository(
    private val api: AuthApi = RetrofitClient.authApi,
    private val localDataSource: LocalUserDataSource = LocalUserDataSource.shared,
    private val sessionStorage: SessionStorage = InMemorySessionStorage(defaultAppMode = AppMode.REMOTE_GATEWAY),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {

    val sessionState: StateFlow<PersistedSession> = sessionStorage.sessionState
    val isSessionLoaded: StateFlow<Boolean> = sessionStorage.isLoaded

    val appMode: AppMode
        get() = sessionStorage.currentAppMode

    init {
        scope.launch {
            sessionStorage.sessionState.collect { session ->
                RetrofitClient.token = if (session.appMode == AppMode.REMOTE_GATEWAY) {
                    session.token
                } else {
                    null
                }
            }
        }
    }

    suspend fun setAppMode(mode: AppMode) {
        sessionStorage.setAppMode(mode)
        if (mode == AppMode.LOCAL_DEMO) {
            RetrofitClient.token = null
        } else {
            RetrofitClient.token = sessionStorage.currentToken
        }
    }

    suspend fun login(email: String, pass: String): User? {
        return when (appMode) {
            AppMode.LOCAL_DEMO -> {
                val user = localDataSource.authenticate(email, pass) ?: return null
                RetrofitClient.token = null
                sessionStorage.saveUserSession(user, token = "local-demo-token-${user.id}")
                user
            }

            AppMode.REMOTE_GATEWAY -> {
                try {
                    val respuesta = api.login(LoginBody(email, pass))
                    val dto = respuesta.usuario
                    val user = User(dto.id, dto.nombre, dto.email, UserRole.valueOf(dto.rol))
                    RetrofitClient.token = respuesta.token
                    sessionStorage.saveUserSession(user, respuesta.token)
                    user
                } catch (_: HttpException) {
                    null
                }
            }
        }
    }

    suspend fun recuperar(email: String): Boolean {
        return when (appMode) {
            AppMode.LOCAL_DEMO -> localDataSource.existsByEmail(email)
            AppMode.REMOTE_GATEWAY -> {
                try {
                    api.recuperar(RecuperarBody(email))
                    true
                } catch (_: HttpException) {
                    false
                }
            }
        }
    }

    fun getCurrentUserOrNull(): User? = sessionStorage.currentUserOrNull

    fun getCurrentUser(): User {
        return getCurrentUserOrNull() ?: User(2, "Captador", "", UserRole.CAPTADOR)
    }

    fun logout() {
        RetrofitClient.token = null
        // UNDISPATCHED ejecuta la limpieza en memoria de forma inmediata antes del primer punto de suspensión.
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            sessionStorage.clearUserSession()
        }
    }

    suspend fun logoutSuspend() {
        RetrofitClient.token = null
        sessionStorage.clearUserSession()
    }
}
