package com.example.servigov2.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * La base de datos local de la app (Room). Tiene una sola tabla: servicios.
 *
 * Usa el patron "singleton": existe una sola instancia en toda la app. Abrir la base
 * varias veces es costoso y puede dar errores, por eso la guardamos y la reutilizamos.
 */
/*
  version = 2 porque al principio la base tenia dos tablas (servicios y farmacias)
  y ahora tiene una sola. Si cambias las tablas TENES que subir este numero:
  si no, Room detecta que el esquema no coincide y la app se cierra al abrir.
*/
@Database(
    entities = [ServiceEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun serviceDao(): ServiceDao

    companion object {
        // @Volatile: asegura que todos los hilos vean el mismo valor actualizado.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            // Si ya existe la devolvemos; si no, la creamos una sola vez.
            // synchronized evita que dos hilos la creen al mismo tiempo.
            return INSTANCE ?: synchronized(this) {
                val nueva = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "servigo.db"
                )
                    // Si cambiamos las tablas (agregamos o sacamos un campo), Room
                    // normalmente tira error y la app se cierra al abrir.
                    // Con esto, en vez de fallar, borra la base vieja y la crea de cero.
                    // Aca no perdemos nada: lo que guardamos es una copia de la API,
                    // asi que se vuelve a descargar sola en la proxima busqueda.
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = nueva
                nueva
            }
        }
    }
}
