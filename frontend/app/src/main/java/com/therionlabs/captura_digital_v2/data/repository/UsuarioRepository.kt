package com.therionlabs.captura_digital_v2.data.repository

import com.therionlabs.captura_digital_v2.data.model.User
import com.therionlabs.captura_digital_v2.data.model.UserRole
import com.therionlabs.captura_digital_v2.data.remote.AuthApi
import com.therionlabs.captura_digital_v2.data.remote.RetrofitClient
import com.therionlabs.captura_digital_v2.data.remote.UsuarioBody
import retrofit2.HttpException

class UsuarioRepository(
    private val api: AuthApi = RetrofitClient.authApi
) {

    suspend fun listar(): List<User> {
        return api.listarUsuarios().map {
            User(it.id, it.nombre, it.email, UserRole.valueOf(it.rol))
        }
    }

    suspend fun crear(nombre: String, email: String, password: String, rol: UserRole): String? {
        return try {
            api.crearUsuario(UsuarioBody(nombre, email, password, rol.name))
            null
        } catch (e: HttpException) {
            if (e.code() == 409) "El correo ya está registrado" else "No se pudo crear el usuario"
        }
    }

    suspend fun actualizar(id: Int, nombre: String, email: String, password: String, rol: UserRole): String? {
        return try {
            api.actualizarUsuario(id, UsuarioBody(nombre, email, password, rol.name))
            null
        } catch (e: HttpException) {
            if (e.code() == 409) "El correo ya está registrado" else "No se pudo actualizar el usuario"
        }
    }

    suspend fun eliminar(id: Int): String? {
        return try {
            api.eliminarUsuario(id)
            null
        } catch (e: HttpException) {
            "No se pudo eliminar el usuario"
        }
    }
}