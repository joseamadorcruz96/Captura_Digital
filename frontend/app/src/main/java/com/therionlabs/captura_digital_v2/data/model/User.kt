package com.therionlabs.captura_digital_v2.data.model

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: UserRole
)