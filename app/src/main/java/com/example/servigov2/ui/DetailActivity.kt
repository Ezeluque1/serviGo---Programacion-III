package com.example.servigov2.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.servigov2.R
import com.example.servigov2.data.ServiceEntity
import com.example.servigov2.data.ServiceRepository
import com.example.servigov2.databinding.ActivityDetalleBinding
import kotlinx.coroutines.launch

/**
 * Pantalla de detalle de un servicio.
 *
 * No recibe el servicio entero por Intent: recibe solo el id (un String) y despues
 * lo busca en Room. Asi los datos salen siempre de la base de datos local y la
 * pantalla funciona igual sin conexion.
 */
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleBinding
    private lateinit var repositorio: ServiceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repositorio = ServiceRepository(this)

        binding.botonVolver.setOnClickListener { finish() }

        val id = intent.getStringExtra(EXTRA_ID_SERVICIO)
        if (id.isNullOrBlank()) {
            mostrarErrorYSalir()
            return
        }

        // Buscamos el servicio en Room dentro de una corrutina (hilo de fondo).
        lifecycleScope.launch {
            val servicio = repositorio.obtenerPorId(id)
            if (servicio == null) {
                mostrarErrorYSalir()
            } else {
                mostrarServicio(servicio)
            }
        }
    }

    /** Llena la pantalla. Cada dato opcional se muestra SOLO si tiene valor. */
    private fun mostrarServicio(servicio: ServiceEntity) {
        binding.nombreDetalle.text = servicio.nombre

        // Nombre del negocio: lo mostramos solo si aporta algo distinto al nombre.
        val negocio = servicio.negocioNombre
        if (!negocio.isNullOrBlank() && negocio != servicio.nombre) {
            binding.negocioDetalle.visibility = View.VISIBLE
            binding.negocioDetalle.text = negocio
        }

        // Rating (la API lo manda en null en muchos negocios)
        if (servicio.rating != null) {
            binding.contenedorRatingDetalle.visibility = View.VISIBLE
            binding.ratingDetalle.text = servicio.rating.toString()

            if (servicio.totalRatings != null) {
                binding.reseniasDetalle.visibility = View.VISIBLE
                binding.reseniasDetalle.text =
                    getString(R.string.formato_resenias, servicio.totalRatings.toString())
            } else {
                binding.reseniasDetalle.visibility = View.GONE
            }
        }

        // Precio
        if (!servicio.precio.isNullOrBlank()) {
            binding.contenedorPrecioDetalle.visibility = View.VISIBLE
            binding.precioDetalle.text = servicio.precio
        }

        // Descripcion (titulo y texto van juntos)
        if (!servicio.descripcion.isNullOrBlank()) {
            binding.tituloDescripcion.visibility = View.VISIBLE
            binding.descripcionDetalle.visibility = View.VISIBLE
            binding.descripcionDetalle.text = servicio.descripcion
        }

        // Direccion: si no vino usamos la ubicacion
        val direccion = servicio.direccion ?: servicio.ubicacion
        if (!direccion.isNullOrBlank()) {
            binding.contenedorDireccion.visibility = View.VISIBLE
            binding.direccionDetalle.text = direccion
        }

        // Imagen con placeholder por si falla o no hay conexion
        Glide.with(this)
            .load(servicio.urlDeImagen())
            .placeholder(R.drawable.placeholder_servicio)
            .error(R.drawable.placeholder_servicio)
            .centerCrop()
            .into(binding.imagenDetalle)

        configurarBotonesDeContacto(servicio)
    }

    /**
     * Muestra los botones de contacto solo si hay dato real.
     * Ojo: la API no deja el telefono vacio, manda el texto "Telefono no disponible".
     * Por eso usamos tieneTelefono() en vez de preguntar si es null.
     */
    private fun configurarBotonesDeContacto(servicio: ServiceEntity) {
        val hayTelefono = servicio.tieneTelefono()
        val hayEmail = servicio.tieneEmail()

        if (hayTelefono) {
            binding.botonLlamar.visibility = View.VISIBLE
            binding.botonLlamar.setOnClickListener {
                // ACTION_DIAL abre el marcador con el numero cargado (no llama solo,
                // asi no hace falta pedir permiso de llamadas).
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:" + servicio.telefono)
                startActivity(intent)
            }
        }

        if (hayEmail) {
            binding.botonEmail.visibility = View.VISIBLE
            binding.botonEmail.setOnClickListener {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto:" + servicio.emailContacto)
                startActivity(intent)
            }
        }

        if (hayTelefono || hayEmail) {
            binding.barraAcciones.visibility = View.VISIBLE
        } else {
            // Sin telefono ni email: no dejamos botones vacios, ponemos un texto neutro.
            binding.barraAcciones.visibility = View.GONE
            binding.sinContacto.visibility = View.VISIBLE
        }
    }

    private fun mostrarErrorYSalir() {
        Toast.makeText(this, R.string.servicio_no_encontrado, Toast.LENGTH_SHORT).show()
        finish()
    }

    companion object {
        /** Nombre de la "etiqueta" con la que viaja el id entre pantallas. */
        const val EXTRA_ID_SERVICIO = "id_servicio"
    }
}
