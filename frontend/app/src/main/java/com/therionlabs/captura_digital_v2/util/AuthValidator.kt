package com.therionlabs.captura_digital_v2.util

object AuthValidator {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validarEmail(email: String): String? = when {
        email.isBlank() -> "Ingresa el correo"
        !EMAIL_REGEX.matches(email) -> "Correo inválido"
        else -> null
    }

    fun validarPassword(password: String): String? = when {
        password.isBlank() -> "Ingresa la contraseña"
        password.length < 6 -> "Debe tener al menos 6 caracteres"
        else -> null
    }

    fun validarNombre(nombre: String): String? =
        if (nombre.isBlank()) "Ingresa el nombre" else null
}