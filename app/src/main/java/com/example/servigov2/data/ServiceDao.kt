package com.example.servigov2.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Esto es el DAO (Data Access Object): aca se definen las consultas a la base de datos.
 * Room escribe solo el codigo que ejecuta estos SELECT e INSERT.
 *
 * Las funciones que devuelven Flow son "observables": cuando cambian los datos de la
 * tabla, Room avisa automaticamente y la pantalla se vuelve a dibujar sola.
 */
@Dao
interface ServiceDao {

    // Trae todos los servicios guardados, ordenados de la A a la Z.
    @Query("SELECT * FROM servicios ORDER BY nombre ASC")
    fun getAll(): Flow<List<ServiceEntity>>

    /**
     * Filtra por texto usando LIKE. El '%' significa "cualquier cosa antes o despues",
     * asi que buscar "plom" encuentra "Plomero Gasista".
     * Busca en el nombre, en la descripcion y en la direccion.
     */
    @Query(
        """
        SELECT * FROM servicios
        WHERE nombre LIKE '%' || :query || '%'
           OR descripcion LIKE '%' || :query || '%'
           OR direccion LIKE '%' || :query || '%'
        ORDER BY nombre ASC
        """
    )
    fun search(query: String): Flow<List<ServiceEntity>>

    // Trae un solo servicio por su id. Lo usa la pantalla de detalle.
    @Query("SELECT * FROM servicios WHERE id = :id")
    suspend fun getById(id: String): ServiceEntity?

    /**
     * Guarda una lista completa. REPLACE significa que si ya existe un servicio
     * con el mismo id, lo pisa con los datos nuevos en vez de dar error.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<ServiceEntity>)

    // Sirve para saber si es la primera vez que se abre la app (base vacia).
    @Query("SELECT COUNT(*) FROM servicios")
    suspend fun contar(): Int
}
