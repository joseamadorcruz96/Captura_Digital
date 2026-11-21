package com.therionlabs.captura_digital_v2.data.remote

import retrofit2.http.GET

data class Indicador(
    val nombre: String,
    val valor: Double
)

data class IndicadoresResponse(
    val uf: Indicador,
    val dolar: Indicador,
    val utm: Indicador
)

interface IndicadoresApi {

    @GET("api")
    suspend fun obtener(): IndicadoresResponse
}