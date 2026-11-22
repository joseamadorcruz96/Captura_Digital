package com.therionlabs.captura_digital_v2.data.remote

import com.therionlabs.captura_digital_v2.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    /**
     * Lee GATEWAY_BASE_URL desde BuildConfig:
     * - Por defecto apunta a la EC2 del equipo en puerto 8080 (Gateway).
     * - En desarrollo local con emulador se sobreescribe en frontend/local.properties (gitignored).
     */
    private val GATEWAY_BASE_URL: String = BuildConfig.GATEWAY_BASE_URL

    var token: String? = null

    private val gatewayHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                token?.let { requestBuilder.header("Authorization", "Bearer $it") }
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
