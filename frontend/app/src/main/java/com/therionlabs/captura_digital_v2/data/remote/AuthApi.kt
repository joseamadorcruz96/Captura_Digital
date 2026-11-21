package com.therionlabs.captura_digital_v2.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

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

    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(@Path("id") id: Int, @Body body: UsuarioBody): UsuarioDto

    @DELETE("api/usuarios/{id}")
    suspend fun eliminarUsuario(@Path("id") id: Int)
}