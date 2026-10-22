package com.example.servigov2.data

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Aca se declaran los endpoints de la API. Retrofit lee esta interfaz y genera
 * solo el codigo que hace las llamadas por internet.
 *
 * "suspend" significa que la funcion se ejecuta dentro de una corrutina, o sea
 * fuera del hilo principal, para que la pantalla no se trabe mientras espera.
 */
interface ApiService {

    // GET https://buscaya.com.ar/api/hybrid-search-android?q=plomero
    @GET("api/hybrid-search-android")
    suspend fun buscarServicios(@Query("q") consulta: String): SearchResponse
}
