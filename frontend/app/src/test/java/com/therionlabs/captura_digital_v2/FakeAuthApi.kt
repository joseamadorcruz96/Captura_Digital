package com.therionlabs.captura_digital_v2

import com.therionlabs.captura_digital_v2.data.remote.AuthApi
import com.therionlabs.captura_digital_v2.data.remote.LoginBody
import com.therionlabs.captura_digital_v2.data.remote.RecuperarBody
import com.therionlabs.captura_digital_v2.data.remote.UsuarioBody
import com.therionlabs.captura_digital_v2.data.remote.UsuarioDto
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class FakeAuthApi : AuthApi {

    val usuarios = mutableListOf(
        UsuarioDto(1, "Administrador", "admin@captura.cl", "ADMINISTRATIVO", true),
        UsuarioDto(2, "Captador Uno", "captador@captura.cl", "CAPTADOR", true)
    )
    var password = "123456"
    var sinConexion = false
    var errorServidor = false

    private fun revisar() {
        if (sinConexion) throw IOException("Sin conexión")
        if (errorServidor) throw http(500)
    }

    private fun http(codigo: Int) = HttpException(Response.error<Any>(codigo, "".toResponseBody(null)))

    override suspend fun login(body: LoginBody): UsuarioDto {
        revisar()
        return usuarios.firstOrNull { it.email == body.email && body.password == password }
            ?: throw http(401)
    }

    override suspend fun recuperar(body: RecuperarBody) {
        revisar()
        if (usuarios.none { it.email == body.email }) throw http(404)
    }

    override suspend fun listarUsuarios(): List<UsuarioDto> {
        revisar()
        return usuarios.toList()
    }

    override suspend fun crearUsuario(body: UsuarioBody): UsuarioDto {
        revisar()
        if (usuarios.any { it.email == body.email }) throw http(409)
        val nuevo = UsuarioDto((usuarios.maxOfOrNull { it.id } ?: 0) + 1, body.nombre, body.email, body.rol, true)
        usuarios.add(nuevo)
        return nuevo
    }

    override suspend fun actualizarUsuario(id: Int, body: UsuarioBody): UsuarioDto {
        revisar()
        val index = usuarios.indexOfFirst { it.id == id }
        if (index == -1) throw http(404)
        if (usuarios.any { it.email == body.email && it.id != id }) throw http(409)
        val actualizado = UsuarioDto(id, body.nombre, body.email, body.rol, true)
        usuarios[index] = actualizado
        return actualizado
    }

    override suspend fun eliminarUsuario(id: Int) {
        revisar()
        if (!usuarios.removeIf { it.id == id }) throw http(404)
    }
}
