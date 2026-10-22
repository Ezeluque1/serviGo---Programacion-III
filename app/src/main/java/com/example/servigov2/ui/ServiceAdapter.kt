package com.example.servigov2.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.servigov2.R
import com.example.servigov2.data.ServiceEntity
import com.example.servigov2.databinding.ItemServicioBinding

/**
 * El Adapter es el puente entre la lista de datos y el RecyclerView:
 * agarra cada ServiceEntity y la dibuja dentro de item_servicio.xml.
 *
 * Usamos ListAdapter (en vez de RecyclerView.Adapter comun) porque trae DiffUtil:
 * cuando la lista cambia, compara la vieja con la nueva y redibuja SOLO los items
 * que cambiaron. Por eso la busqueda se siente fluida y no parpadea.
 *
 * @param alTocarServicio funcion que se ejecuta cuando el usuario toca una tarjeta.
 */
class ServiceAdapter(
    private val alTocarServicio: (ServiceEntity) -> Unit
) : ListAdapter<ServiceEntity, ServiceAdapter.ServicioViewHolder>(DIFF_CALLBACK) {

    /** El ViewHolder guarda las vistas de UNA tarjeta para no buscarlas cada vez. */
    inner class ServicioViewHolder(
        private val binding: ItemServicioBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun mostrar(servicio: ServiceEntity) {
            binding.nombreServicio.text = servicio.nombre

            // Direccion: si no vino, usamos la ubicacion. Si tampoco, ocultamos la fila.
            val direccion = servicio.direccion ?: servicio.ubicacion
            if (direccion.isNullOrBlank()) {
                binding.direccionServicio.visibility = android.view.View.GONE
            } else {
                binding.direccionServicio.visibility = android.view.View.VISIBLE
                binding.direccionServicio.text = direccion
            }

            // Rating: la API lo manda en null en muchos negocios, entonces ocultamos todo.
            if (servicio.rating == null) {
                binding.contenedorRating.visibility = android.view.View.GONE
            } else {
                binding.contenedorRating.visibility = android.view.View.VISIBLE
                binding.ratingServicio.text = servicio.rating.toString()

                if (servicio.totalRatings == null) {
                    binding.reseniasServicio.visibility = android.view.View.GONE
                } else {
                    binding.reseniasServicio.visibility = android.view.View.VISIBLE
                    binding.reseniasServicio.text = itemView.context.getString(
                        R.string.formato_resenias,
                        servicio.totalRatings.toString()
                    )
                }
            }

            // Precio
            if (servicio.precio.isNullOrBlank()) {
                binding.contenedorPrecio.visibility = android.view.View.INVISIBLE
            } else {
                binding.contenedorPrecio.visibility = android.view.View.VISIBLE
                binding.precioServicio.text = servicio.precio
            }

            // Imagen con Glide. El placeholder se ve mientras carga y tambien
            // si falla (por ejemplo en modo avion).
            Glide.with(itemView.context)
                .load(servicio.urlDeImagen())
                .placeholder(R.drawable.placeholder_servicio)
                .error(R.drawable.placeholder_servicio)
                .centerCrop()
                .into(binding.imagenServicio)

            // Se puede tocar toda la tarjeta o el boton
            itemView.setOnClickListener { alTocarServicio(servicio) }
            binding.botonDetalle.setOnClickListener { alTocarServicio(servicio) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioViewHolder {
        val binding = ItemServicioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ServicioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServicioViewHolder, position: Int) {
        holder.mostrar(getItem(position))
    }

    companion object {
        /**
         * DiffUtil le explica al adapter como comparar dos servicios:
         * - areItemsTheSame: son el mismo servicio? (comparamos el id)
         * - areContentsTheSame: cambio algun dato adentro? (comparamos todo)
         */
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ServiceEntity>() {
            override fun areItemsTheSame(a: ServiceEntity, b: ServiceEntity): Boolean {
                return a.id == b.id
            }

            override fun areContentsTheSame(a: ServiceEntity, b: ServiceEntity): Boolean {
                return a == b
            }
        }
    }
}
