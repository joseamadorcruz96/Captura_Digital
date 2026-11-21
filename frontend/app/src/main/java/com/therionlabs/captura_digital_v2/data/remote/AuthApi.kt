package com.therionlabs.captura_digital_v2.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

data class LoginBody(val email: String, val password: String)

data class RecuperarBody(val email: String)

data class UsuarioDto(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: String,
    val activo: Boolean
)

interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginBody): UsuarioDto

    @POST("api/auth/recuperar")
    suspend fun recuperar(@Body body: RecuperarBody)
}