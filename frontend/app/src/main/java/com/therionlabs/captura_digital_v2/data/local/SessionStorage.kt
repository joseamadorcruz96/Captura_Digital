package com.therionlabs.captura_digital_v2.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.therionlabs.captura_digital_v2.data.model.AppMode
import com.therionlabs.captura_digital_v2.data.model.User
import com.therionlabs.captura_digital_v2.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException

private val Context.capturaSessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "captura_digital_session"
)

/**
 * Estado persistido de sesión y modo operativo de la aplicación.
 */
data class PersistedSession(
    val user: User? = null,
    val token: String? = null,
    val appMode: AppMode = AppMode.LOCAL_DEMO
)

/**
 * Abstracción de almacenamiento de sesión para permitir DataStore real en Android
 * e implementación en memoria determinista en pruebas unitarias.
 */
interface SessionStorage {
    val sessionState: StateFlow<PersistedSession>
    val isLoaded: StateFlow<Boolean>

    val currentAppMode: AppMode
        get() = sessionState.value.appMode

    val currentUserOrNull: User?
        get() = sessionState.value.user

    val currentToken: String?
        get() = sessionState.value.token

    suspend fun saveUserSession(user: User, token: String?)
    suspend fun clearUserSession()
    suspend fun setAppMode(mode: AppMode)
}

/**
 * Implementación en memoria para pruebas unitarias o fallback sin Context.
 */
class InMemorySessionStorage(
    defaultAppMode: AppMode = AppMode.LOCAL_DEMO,
    initialUser: User? = null,
    initialToken: String? = null
) : SessionStorage {

    private val _sessionState = MutableStateFlow(
        PersistedSession(user = initialUser, token = initialToken, appMode = defaultAppMode)
    )
    override val sessionState: StateFlow<PersistedSession> = _sessionState.asStateFlow()

    private val _isLoaded = MutableStateFlow(true)
    override val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    override suspend fun saveUserSession(user: User, token: String?) {
        _sessionState.value = _sessionState.value.copy(user = user, token = token)
    }

    override suspend fun clearUserSession() {
        _sessionState.value = _sessionState.value.copy(user = null, token = null)
    }

    override suspend fun setAppMode(mode: AppMode) {
        _sessionState.value = _sessionState.value.copy(appMode = mode)
    }
}

/**
 * Implementación persistente basada en Preferences DataStore.
 * Mantiene en memoria un StateFlow sincronizado para lecturas reactivas y síncronas.
 */
class DataStoreSessionStorage(
    private val dataStore: DataStore<Preferences>,
    private val defaultAppMode: AppMode = AppMode.LOCAL_DEMO,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : SessionStorage {

    private val _sessionState = MutableStateFlow(PersistedSession(appMode = defaultAppMode))
    override val sessionState: StateFlow<PersistedSession> = _sessionState.asStateFlow()

    private val _isLoaded = MutableStateFlow(false)
    override val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    init {
        scope.launch {
            dataStore.data
                .catch { ex ->
                    if (ex is IOException) emit(emptyPreferences()) else throw ex
                }
                .collect { prefs ->
                    _sessionState.value = prefs.toPersistedSession(defaultAppMode)
                    _isLoaded.value = true
                }
        }
    }

    /**
     * Espera hasta que el estado inicial haya sido cargado desde disco.
     */
    suspend fun awaitInitialLoad(): PersistedSession {
        if (!_isLoaded.value) {
            val prefs = dataStore.data
                .catch { ex ->
                    if (ex is IOException) emit(emptyPreferences()) else throw ex
                }
                .first()
            _sessionState.value = prefs.toPersistedSession(defaultAppMode)
            _isLoaded.value = true
        }
        return _sessionState.value
    }

    override suspend fun saveUserSession(user: User, token: String?) {
        _sessionState.value = _sessionState.value.copy(user = user, token = token)
        dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = user.id
            prefs[KEY_USER_NAME] = user.name
            prefs[KEY_USER_EMAIL] = user.email
            prefs[KEY_USER_ROLE] = user.role.name
            if (token != null) {
                prefs[KEY_TOKEN] = token
            } else {
                prefs.remove(KEY_TOKEN)
            }
        }
    }

    override suspend fun clearUserSession() {
        _sessionState.value = _sessionState.value.copy(user = null, token = null)
        dataStore.edit { prefs ->
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USER_NAME)
            prefs.remove(KEY_USER_EMAIL)
            prefs.remove(KEY_USER_ROLE)
            prefs.remove(KEY_TOKEN)
        }
    }

    override suspend fun setAppMode(mode: AppMode) {
        _sessionState.value = _sessionState.value.copy(appMode = mode)
        dataStore.edit { prefs ->
            prefs[KEY_APP_MODE] = mode.name
        }
    }

    private fun Preferences.toPersistedSession(fallbackMode: AppMode): PersistedSession {
        val mode = this[KEY_APP_MODE]
            ?.let { raw -> runCatching { AppMode.valueOf(raw) }.getOrNull() }
            ?: fallbackMode

        val userId = this[KEY_USER_ID]
        val userName = this[KEY_USER_NAME]
        val userEmail = this[KEY_USER_EMAIL]
        val userRole = this[KEY_USER_ROLE]?.let { raw ->
            runCatching { UserRole.valueOf(raw) }.getOrNull()
        }

        val user = if (userId != null && userName != null && userEmail != null && userRole != null) {
            User(id = userId, name = userName, email = userEmail, role = userRole)
        } else {
            null
        }

        val token = if (user != null) this[KEY_TOKEN] else null
        return PersistedSession(user = user, token = token, appMode = mode)
    }

    companion object {
        private val KEY_USER_ID = intPreferencesKey("session_user_id")
        private val KEY_USER_NAME = stringPreferencesKey("session_user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("session_user_email")
        private val KEY_USER_ROLE = stringPreferencesKey("session_user_role")
        private val KEY_TOKEN = stringPreferencesKey("session_token")
        private val KEY_APP_MODE = stringPreferencesKey("app_mode")

        fun create(
            context: Context,
            defaultAppMode: AppMode = AppMode.LOCAL_DEMO
        ): DataStoreSessionStorage {
            return DataStoreSessionStorage(
                dataStore = context.applicationContext.capturaSessionDataStore,
                defaultAppMode = defaultAppMode
            )
        }
    }
}
