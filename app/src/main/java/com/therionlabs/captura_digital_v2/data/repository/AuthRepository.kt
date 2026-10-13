package com.therionlabs.captura_digital_v2.data.repository

import com.therionlabs.captura_digital_v2.data.model.User
import com.therionlabs.captura_digital_v2.data.model.UserRole

class AuthRepository {

    companion object {
        private var currentUser: User? = null
    }

    private val users = listOf(
        User(1, "Administrador", "admin@captura.cl", UserRole.ADMINISTRATIVO),
        User(2, "Captador Uno", "captador@captura.cl", UserRole.CAPTADOR)
    )

    private val password = "123456"

    fun login(email: String, pass: String): User? {
        val user = users.find { it.email == email && pass == password }
        if (user != null) {
            currentUser = user
        }
        return user
    }

    fun getCurrentUser(): User {
        // Retorna el usuario autenticado o el usuario por defecto (Captador) para pruebas
        return currentUser ?: users.first { it.role == UserRole.CAPTADOR }
    }

    fun logout() {
        currentUser = null
    }

    fun emailExists(email: String): Boolean {
        return users.any { it.email == email }
    }
}