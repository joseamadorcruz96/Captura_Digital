package com.therionlabs.captura_digital_v2.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://100.61.210.112:8081/"

    var token: String? = null

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val peticion = chain.request().newBuilder()
            token?.let { peticion.addHeader("Authorization", "Bearer $it") }
            chain.proceed(peticion.build())
        }
        .build()

    val authApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }

    val indicadoresApi: IndicadoresApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://mindicador.cl/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IndicadoresApi::class.java)
    }
}