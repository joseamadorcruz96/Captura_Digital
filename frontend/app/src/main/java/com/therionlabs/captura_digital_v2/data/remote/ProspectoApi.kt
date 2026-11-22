package com.therionlabs.captura_digital_v2.data.remote

import com.therionlabs.captura_digital_v2.data.model.dto.BatchSyncRequestDto
import com.therionlabs.captura_digital_v2.data.model.dto.BatchSyncResponseDto
import com.therionlabs.captura_digital_v2.data.model.dto.DashboardResumenDto
import com.therionlabs.captura_digital_v2.data.model.dto.EventoResponseDto
import com.therionlabs.captura_digital_v2.data.model.dto.ProspectoRequestDto
import com.therionlabs.captura_digital_v2.data.model.dto.ProspectoResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Contrato Retrofit hacia ms-formulario a través del API Gateway (/api/v1/prospectos y /api/v1/eventos).
 */
interface ProspectoApi {

    @GET("api/v1/prospectos")
    suspend fun listar(
        @Query("captadorId") captadorId: Int? = null,
        @Query("eventoId") eventoId: String? = null,
        @Query("rut") rut: String? = null,
        @Query("sede") sede: String? = null
    ): List<ProspectoResponseDto>

    @GET("api/v1/prospectos/{clientUuid}")
    suspend fun obtener(@Path("clientUuid") clientUuid: String): ProspectoResponseDto

    @POST("api/v1/prospectos")
    suspend fun crear(@Body body: ProspectoRequestDto): ProspectoResponseDto

    @PUT("api/v1/prospectos/{clientUuid}")
    suspend fun actualizar(
        @Path("clientUuid") clientUuid: String,
        @Body body: ProspectoRequestDto
    ): ProspectoResponseDto

    @DELETE("api/v1/prospectos/{clientUuid}")
    suspend fun eliminar(@Path("clientUuid") clientUuid: String)

    @POST("api/v1/prospectos/sync")
    suspend fun sincronizarLote(@Body body: BatchSyncRequestDto): BatchSyncResponseDto

    @GET("api/v1/prospectos/resumen")
    suspend fun obtenerResumen(@Query("eventoId") eventoId: String? = null): DashboardResumenDto

    @GET("api/v1/eventos")
    suspend fun listarEventos(@Query("activo") activo: Boolean? = true): List<EventoResponseDto>
}
