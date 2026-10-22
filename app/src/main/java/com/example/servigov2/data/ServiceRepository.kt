package com.example.servigov2.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Resultado de intentar actualizar los datos desde la API.
 * Lo usa la pantalla para saber si tiene que mostrar algun aviso.
 */
enum class ResultadoActualizacion {
    OK,                 // se trajo todo bien
    SIN_CONEXION,       // fallo la red: seguimos mostrando lo guardado en Room
    LIMITE_ALCANZADO    // la API aviso que se acabaron las busquedas del dia
}

/**
 * El repositorio es el intermediario entre la pantalla y los datos.
 *
 * REGLA IMPORTANTE: Room es la unica fuente de verdad.
 * La pantalla NUNCA lee de internet directamente. El flujo siempre es:
 *   API -> se guarda en Room -> la pantalla lee de Room
 * Gracias a eso la app funciona sin conexion: si no hay internet simplemente
 * no se actualiza Room, pero lo que ya estaba guardado se sigue viendo.
 */
class ServiceRepository(context: Context) {

    private val baseDeDatos = AppDatabase.getInstance(context)
    private val servicioDao = baseDeDatos.serviceDao()
    private val api = RetrofitClient.api

    // ---------- LECTURA: siempre desde Room ----------

    /** Todos los servicios guardados. Devuelve Flow: se actualiza solo. */
    fun obtenerTodos(): Flow<List<ServiceEntity>> = servicioDao.getAll()

    /** Filtra con LIKE los servicios ya guardados. Funciona sin internet. */
    fun buscarGuardados(texto: String): Flow<List<ServiceEntity>> = servicioDao.search(texto)

    /** Trae un servicio puntual para la pantalla de detalle. */
    suspend fun obtenerPorId(id: String): ServiceEntity? = withContext(Dispatchers.IO) {
        servicioDao.getById(id)
    }

    /** Sirve para saber si es la primera vez que se abre la app. */
    suspend fun hayServiciosGuardados(): Boolean = withContext(Dispatchers.IO) {
        servicioDao.contar() > 0
    }

    // ---------- ESCRITURA: traer de la API y guardar en Room ----------

    /**
     * Llama a la API con el texto buscado y guarda los resultados en Room.
     * No devuelve la lista: la pantalla la recibe sola por el Flow de Room.
     *
     * withContext(Dispatchers.IO) manda este trabajo a un hilo de fondo,
     * para no congelar la pantalla mientras se espera al servidor.
     */
    suspend fun actualizarDesdeApi(consulta: String): ResultadoActualizacion =
        withContext(Dispatchers.IO) {
            // La API rechaza las busquedas vacias, asi que ni la llamamos.
            if (consulta.isBlank()) return@withContext ResultadoActualizacion.OK

            try {
                val respuesta = api.buscarServicios(consulta)

                if (respuesta.limiteDiarioAlcanzado == true) {
                    return@withContext ResultadoActualizacion.LIMITE_ALCANZADO
                }

                // Si la API devolvio un error, "results" viene en null.
                val resultados = respuesta.results ?: emptyList()
                val servicios = resultados
                    .filter { !it.id.isNullOrBlank() }   // sin id no lo podemos guardar
                    .map { convertirAEntity(it) }

                if (servicios.isNotEmpty()) {
                    servicioDao.insertAll(servicios)
                }
                ResultadoActualizacion.OK
            } catch (e: Exception) {
                // Sin internet (modo avion) o error del servidor: no rompemos nada.
                // La pantalla sigue mostrando lo que ya estaba guardado en Room.
                ResultadoActualizacion.SIN_CONEXION
            }
        }

    // ---------- Conversion ----------

    /** Pasa un objeto que vino del JSON (DTO) a una fila de la base de datos (Entity). */
    private fun convertirAEntity(dto: ServiceDto): ServiceEntity {
        return ServiceEntity(
            id = dto.id ?: "",
            nombre = dto.nombre ?: "",
            descripcion = dto.descripcion,
            precio = dto.precio,
            imagen = dto.imagen,
            negocioNombre = dto.negocioNombre,
            emailContacto = dto.emailContacto,
            telefono = dto.telefono,
            fuente = dto.fuente,
            ubicacion = dto.ubicacion,
            direccion = dto.direccion,
            rating = dto.rating,
            totalRatings = dto.totalRatings,
            placeId = dto.placeId
        )
    }

    companion object {
        /**
         * Texto que mandamos la primera vez que se abre la app, porque la API
         * no acepta busquedas vacias (devuelve {"error": "Query vacia"}).
         * Probamos varias opciones y "servicios" es la que trae resultados mas variados.
         */
        const val CONSULTA_INICIAL = "servicios"
    }
}
