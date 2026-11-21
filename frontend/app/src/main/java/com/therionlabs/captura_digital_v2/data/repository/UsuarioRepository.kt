package com.therionlabs.captura_digital_v2.data.repository

import com.therionlabs.captura_digital_v2.data.model.User
import com.therionlabs.captura_digital_v2.data.model.UserRole
import com.therionlabs.captura_digital_v2.data.remote.RetrofitClient
import com.therionlabs.captura_digital_v2.data.remote.UsuarioBody
import retrofit2.HttpException

class UsuarioRepository {

    private val api = RetrofitClient.authApi

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
}