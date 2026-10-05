package com.therionlabs.captura_digital_v2.data.repository

import com.therionlabs.captura_digital_v2.data.model.User
import com.therionlabs.captura_digital_v2.data.model.UserRole

class AuthRepository {

    private val users = listOf(
        User(1, "Administrador", "admin@captura.cl", UserRole.ADMINISTRATIVO),
        User(2, "Captador Uno", "captador@captura.cl", UserRole.CAPTADOR)
    )

    private val password = "123456"

    fun login(email: String, pass: String): User? {
        return users.find { it.email == email && pass == password }
    }

    fun emailExists(email: String): Boolean {
        return users.any { it.email == email }
    }
}