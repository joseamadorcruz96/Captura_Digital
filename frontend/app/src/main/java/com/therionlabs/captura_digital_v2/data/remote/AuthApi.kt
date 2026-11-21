package com.therionlabs.captura_digital_v2.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class LoginBody(val email: String, val password: String)

data class RecuperarBody(val email: String)

data class UsuarioBody(
    val nombre: String,
    val email: String,
    val password: String,
    val rol: String
)

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

    @GET("api/usuarios")
    suspend fun listarUsuarios(): List<UsuarioDto>

    @POST("api/usuarios")
    suspend fun crearUsuario(@Body body: UsuarioBody): UsuarioDto
}