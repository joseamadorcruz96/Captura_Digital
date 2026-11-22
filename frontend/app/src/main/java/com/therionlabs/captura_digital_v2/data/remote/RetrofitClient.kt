package com.therionlabs.captura_digital_v2.data.remote

import com.therionlabs.captura_digital_v2.data.repository.AuthRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    /**
     * URL única del API Gateway:
     * - Emulador Android contra PC local: "http://10.0.2.2:8080/"
     * - Teléfono físico por USB (adb reverse tcp:8080 tcp:8080): "http://127.0.0.1:8080/"
     * - AWS EC2 / Red local: reemplazar por la URL o IP pública del Gateway.
     */
    private const val GATEWAY_BASE_URL = "http://10.0.2.2:8080/"

    private val gatewayHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val user = AuthRepository.currentSessionUser()
                val requestBuilder = chain.request().newBuilder()
                if (user != null) {
                    requestBuilder
                        .header("X-User-Id", user.id.toString())
                        .header("X-User-Rol", user.role.name)
                }
                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    private val gatewayRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(GATEWAY_BASE_URL)
            .client(gatewayHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy {
        gatewayRetrofit.create(AuthApi::class.java)
    }

    val prospectoApi: ProspectoApi by lazy {
        gatewayRetrofit.create(ProspectoApi::class.java)
    }

    // API Externa (fuera del Gateway, sin persistencia propia)
    val indicadoresApi: IndicadoresApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://mindicador.cl/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IndicadoresApi::class.java)
    }
}
