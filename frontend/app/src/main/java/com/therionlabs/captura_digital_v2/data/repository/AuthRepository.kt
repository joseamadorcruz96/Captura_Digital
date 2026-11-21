package com.therionlabs.captura_digital_v2.data.repository

import com.therionlabs.captura_digital_v2.data.model.User
import com.therionlabs.captura_digital_v2.data.model.UserRole
import com.therionlabs.captura_digital_v2.data.remote.AuthApi
import com.therionlabs.captura_digital_v2.data.remote.LoginBody
import com.therionlabs.captura_digital_v2.data.remote.RecuperarBody
import com.therionlabs.captura_digital_v2.data.remote.RetrofitClient
import retrofit2.HttpException

class AuthRepository(
    private val api: AuthApi = RetrofitClient.authApi
) {

    companion object {
        private var currentUser: User? = null
    }

    suspend fun login(email: String, pass: String): User? {
        return try {
            val dto = api.login(LoginBody(email, pass))
            val user = User(dto.id, dto.nombre, dto.email, UserRole.valueOf(dto.rol))
            currentUser = user
            user
        } catch (e: HttpException) {
            null
        }
    }

    suspend fun recuperar(email: String): Boolean {
        return try {
            api.recuperar(RecuperarBody(email))
            true
        } catch (e: HttpException) {
            false
        }
    }

    fun getCurrentUser(): User {
        return currentUser ?: User(2, "Captador", "", UserRole.CAPTADOR)
    }

    fun logout() {
        currentUser = null
    }
}