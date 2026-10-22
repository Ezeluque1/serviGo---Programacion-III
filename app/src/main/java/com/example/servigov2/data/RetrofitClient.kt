package com.example.servigov2.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Crea y guarda una sola instancia de Retrofit para toda la app (singleton).
 * GsonConverterFactory es el que transforma el JSON del servidor en nuestras data class.
 */
object RetrofitClient {

    const val BASE_URL = "https://buscaya.com.ar/"

    // "by lazy" = se crea recien la primera vez que se usa, y despues se reutiliza.
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
