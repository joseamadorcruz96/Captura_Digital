package com.therionlabs.captura_digital_v2.data.repository

import com.therionlabs.captura_digital_v2.data.local.LocalUserDataSource
import com.therionlabs.captura_digital_v2.data.model.AppMode
import com.therionlabs.captura_digital_v2.data.model.User
import com.therionlabs.captura_digital_v2.data.model.UserRole
import com.therionlabs.captura_digital_v2.data.remote.AuthApi
import com.therionlabs.captura_digital_v2.data.remote.RetrofitClient
import com.therionlabs.captura_digital_v2.data.remote.UsuarioBody
import retrofit2.HttpException

/**
 * Repositorio de usuarios con soporte dual:
 * - [AppMode.LOCAL_DEMO]: CRUD completo sobre [LocalUserDataSource] sin llamadas HTTP.
 * - [AppMode.REMOTE_GATEWAY]: CRUD remoto contra `ms-login` vía [AuthApi].
 */
class UsuarioRepository(
    private val api: AuthApi = RetrofitClient.authApi,
    private val localDataSource: LocalUserDataSource = LocalUserDataSource.shared,
    private val appModeProvider: () -> AppMode = { AppMode.REMOTE_GATEWAY }
) {

    suspend fun listar(): List<User> {
        return when (appModeProvider()) {
            AppMode.LOCAL_DEMO -> localDataSource.listUsers()
            AppMode.REMOTE_GATEWAY -> api.listarUsuarios().map {
                User(it.id, it.nombre, it.email, UserRole.valueOf(it.rol))
            }
        }
    }

    suspend fun crear(nombre: String, email: String, password: String, rol: UserRole): String? {
        return when (appModeProvider()) {
            AppMode.LOCAL_DEMO -> localDataSource.createUser(nombre, email, password, rol)
            AppMode.REMOTE_GATEWAY -> try {
                api.crearUsuario(UsuarioBody(nombre, email, password, rol.name))
                null
            } catch (e: HttpException) {
                if (e.code() == 409) "El correo ya está registrado" else "No se pudo crear el usuario"
            }
        }
    }

    suspend fun actualizar(id: Int, nombre: String, email: String, password: String, rol: UserRole): String? {
        return when (appModeProvider()) {
            AppMode.LOCAL_DEMO -> localDataSource.updateUser(id, nombre, email, password, rol)
            AppMode.REMOTE_GATEWAY -> try {
                api.actualizarUsuario(id, UsuarioBody(nombre, email, password, rol.name))
                null
            } catch (e: HttpException) {
                if (e.code() == 409) "El correo ya está registrado" else "No se pudo actualizar el usuario"
            }
        }
    }

    suspend fun eliminar(id: Int): String? {
        return when (appModeProvider()) {
            AppMode.LOCAL_DEMO -> localDataSource.deleteUser(id)
            AppMode.REMOTE_GATEWAY -> try {
                api.eliminarUsuario(id)
                null
            } catch (_: HttpException) {
                "No se pudo eliminar el usuario"
            }
        }
    }
}
