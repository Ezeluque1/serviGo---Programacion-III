package com.example.servigov2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Esto es una Entity: representa una fila de la tabla "servicios" en la base de datos local.
 * Cada propiedad es una columna. Guardamos aca lo que nos devuelve la API.
 *
 * Los campos que la API puede mandar vacios o nulos estan declarados con "?" (nullable).
 */
@Entity(tableName = "servicios")
data class ServiceEntity(
    // La API manda el id como texto (ej: "google_ChIJ..."), no como numero.
    @PrimaryKey
    val id: String,
    val nombre: String,
    val descripcion: String?,
    val precio: String?,
    val imagen: String?,
    val negocioNombre: String?,
    val emailContacto: String?,
    val telefono: String?,
    val fuente: String?,
    val ubicacion: String?,
    val direccion: String?,
    // El rating llega a veces como 5 y a veces como 4.5, por eso es Double. Puede ser null.
    val rating: Double?,
    val totalRatings: Int?,
    val placeId: String?
) {

    /**
     * La API manda la imagen de dos formas: como URL completa ("https://...")
     * o como ruta relativa ("img/foto.png"). Si es relativa le pegamos adelante
     * la direccion base del servidor.
     */
    fun urlDeImagen(): String? {
        if (imagen.isNullOrBlank()) return null
        return if (imagen.startsWith("http")) imagen else RetrofitClient.BASE_URL + imagen
    }

    /**
     * La API no deja el telefono vacio: cuando no lo tiene manda el texto
     * "Telefono no disponible". Por eso no alcanza con preguntar si es null.
     */
    fun tieneTelefono(): Boolean {
        if (telefono.isNullOrBlank()) return false
        return !telefono.contains("no disponible", ignoreCase = true)
    }

    fun tieneEmail(): Boolean {
        return !emailContacto.isNullOrBlank()
    }
}
