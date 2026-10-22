package com.example.servigov2.data

import com.google.gson.annotations.SerializedName

/**
 * Estas clases representan el JSON tal cual lo manda la API.
 * Gson las llena automaticamente a partir del texto que llega del servidor.
 *
 * @SerializedName se usa cuando el nombre del campo en el JSON es distinto
 * al nombre que queremos en Kotlin (ej: "negocio_nombre" -> negocioNombre).
 */

/** Respuesta de /api/hybrid-search-android */
data class SearchResponse(
    // Es nullable a proposito: si mandamos una busqueda vacia, la API no manda
    // "results" sino {"error": "Query vacia"}. Asi la app no se rompe.
    val results: List<ServiceDto>?,
    val total: Int?,
    @SerializedName("local_count") val localCount: Int?,
    @SerializedName("google_count") val googleCount: Int?,
    val location: String?,
    val message: String?,
    val cache: String?,
    @SerializedName("limite_diario_alcanzado") val limiteDiarioAlcanzado: Boolean?,
    val error: String?
)

/** Cada servicio dentro de "results" */
data class ServiceDto(
    val id: String?,
    val nombre: String?,
    val descripcion: String?,
    val precio: String?,
    val imagen: String?,
    @SerializedName("negocio_nombre") val negocioNombre: String?,
    @SerializedName("email_contacto") val emailContacto: String?,
    val telefono: String?,
    val fuente: String?,
    val ubicacion: String?,
    val direccion: String?,
    val rating: Double?,
    @SerializedName("total_ratings") val totalRatings: Int?,
    @SerializedName("place_id") val placeId: String?
)
