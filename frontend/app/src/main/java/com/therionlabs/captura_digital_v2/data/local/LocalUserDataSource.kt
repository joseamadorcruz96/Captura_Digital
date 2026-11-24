package com.therionlabs.captura_digital_v2.data.local

import com.therionlabs.captura_digital_v2.data.model.User
import com.therionlabs.captura_digital_v2.data.model.UserRole

/**
 * Fuente de datos local en memoria para autenticación y gestión de usuarios en modo LOCAL_DEMO.
 * Incluye cuentas predefinidas para los tres roles del sistema (ADMINISTRATIVO, CAPTADOR, PROSPECTO)
 * y soporta operaciones CRUD thread-safe compartidas entre AuthRepository y UsuarioRepository.
 */
class LocalUserDataSource {

    private data class LocalUserRecord(
        val user: User,
        val password: String
    )

    private val lock = Any()
    private val records = mutableListOf<LocalUserRecord>()

    init {
        resetDefaults()
    }

    fun authenticate(email: String, password: String): User? = synchronized(lock) {
        val normalized = email.trim()
        records.firstOrNull {
            it.user.email.equals(normalized, ignoreCase = true) && it.password == password
        }?.user
    }

    fun existsByEmail(email: String): Boolean = synchronized(lock) {
        val normalized = email.trim()
        records.any { it.user.email.equals(normalized, ignoreCase = true) }
    }

    fun listUsers(): List<User> = synchronized(lock) {
        records.map { it.user }
    }

    fun createUser(
        name: String,
        email: String,
        password: String,
        role: UserRole
    ): String? = synchronized(lock) {
        val normalizedEmail = email.trim()
        if (records.any { it.user.email.equals(normalizedEmail, ignoreCase = true) }) {
            return "El correo ya está registrado"
        }
        val nextId = (records.maxOfOrNull { it.user.id } ?: 0) + 1
        records.add(
            LocalUserRecord(
                user = User(
                    id = nextId,
                    name = name.trim(),
                    email = normalizedEmail,
                    role = role
                ),
                password = password
            )
        )
        null
    }

    fun updateUser(
        id: Int,
        name: String,
        email: String,
        password: String,
        role: UserRole
    ): String? = synchronized(lock) {
        val index = records.indexOfFirst { it.user.id == id }
        if (index == -1) {
            return "No se pudo actualizar el usuario"
        }
        val normalizedEmail = email.trim()
        if (records.any { it.user.id != id && it.user.email.equals(normalizedEmail, ignoreCase = true) }) {
            return "El correo ya está registrado"
        }
        records[index] = LocalUserRecord(
            user = User(
                id = id,
                name = name.trim(),
                email = normalizedEmail,
                role = role
            ),
            password = password
        )
        null
    }

    fun deleteUser(id: Int): String? = synchronized(lock) {
        val removed = records.removeAll { it.user.id == id }
        if (!removed) "No se pudo eliminar el usuario" else null
    }

    fun resetDefaults() = synchronized(lock) {
        records.clear()
        records.addAll(DEFAULT_RECORDS)
    }

    companion object {
        const val DEMO_PASSWORD: String = "123456"

        val DEMO_ADMIN = User(
            id = 1,
            name = "Admin Duoc Demo",
            email = "admin@duoc.cl",
            role = UserRole.ADMINISTRATIVO
        )
        val DEMO_CAPTADOR = User(
            id = 2,
            name = "Captador Terreno Demo",
            email = "captador@duoc.cl",
            role = UserRole.CAPTADOR
        )
        val DEMO_PROSPECTO = User(
            id = 3,
            name = "Kiosco Prospecto Demo",
            email = "prospecto@duoc.cl",
            role = UserRole.PROSPECTO
        )

        private val DEFAULT_RECORDS = listOf(
            LocalUserRecord(DEMO_ADMIN, DEMO_PASSWORD),
            LocalUserRecord(DEMO_CAPTADOR, DEMO_PASSWORD),
            LocalUserRecord(DEMO_PROSPECTO, DEMO_PASSWORD)
        )

        val shared: LocalUserDataSource by lazy { LocalUserDataSource() }
    }
}
