package com.example.servigov2.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.servigov2.R
import com.example.servigov2.databinding.ActivityServiciosBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

/**
 * Pantalla principal: muestra el buscador y la lista de servicios.
 *
 * La Activity NO sabe nada de internet ni de la base de datos. Solo:
 *  1) le avisa al ViewModel lo que el usuario escribe,
 *  2) dibuja la lista que el ViewModel le manda.
 */
class ServicesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServiciosBinding
    private lateinit var adaptador: ServiceAdapter

    // "by viewModels()" crea el ViewModel y lo reutiliza si la pantalla gira.
    private val viewModel: ServicesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiciosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarLista()
        configurarBuscador()
        configurarChips()
        observarDatos()
    }

    /** Prepara el RecyclerView con su adapter. */
    private fun configurarLista() {
        adaptador = ServiceAdapter { servicio ->
            // Abrimos el detalle pasando solo el id; la otra pantalla lo busca en Room.
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(DetailActivity.EXTRA_ID_SERVICIO, servicio.id)
            startActivity(intent)
        }
        binding.listaServicios.layoutManager = LinearLayoutManager(this)
        binding.listaServicios.adapter = adaptador
    }

    /** Avisa al ViewModel cada vez que cambia el texto del buscador. */
    private fun configurarBuscador() {
        binding.campoBusqueda.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}

            override fun afterTextChanged(s: Editable?) {
                viewModel.alCambiarTexto(s.toString())
            }
        })
    }

    /**
     * Crea los chips de categoria a partir de dos listas de strings.xml:
     * una con el texto que se ve y otra con la palabra que se busca.
     * Al tocar un chip escribimos esa palabra en el buscador.
     */
    private fun configurarChips() {
        val nombres = resources.getStringArray(R.array.categorias_nombres)
        val consultas = resources.getStringArray(R.array.categorias_consultas)

        nombres.forEachIndexed { posicion, nombre ->
            val chip = Chip(this)
            chip.text = nombre
            chip.isCheckable = true
            chip.isChecked = posicion == 0      // "Todos" arranca seleccionado
            chip.setChipBackgroundColorResource(R.color.chip_fondo)
            chip.setTextColor(resources.getColorStateList(R.color.chip_texto, theme))
            chip.setChipStrokeColorResource(R.color.chip_borde)
            chip.chipStrokeWidth = resources.getDimension(R.dimen.borde_chip)
            chip.isCheckedIconVisible = false

            chip.setOnClickListener {
                // setText dispara el TextWatcher, que avisa al ViewModel.
                binding.campoBusqueda.setText(consultas[posicion])
                binding.campoBusqueda.setSelection(consultas[posicion].length)
            }
            binding.grupoCategorias.addView(chip)
        }
    }

    /**
     * Escucha los datos del ViewModel.
     *
     * repeatOnLifecycle(STARTED) hace que solo escuchemos mientras la pantalla
     * esta visible. Si el usuario se va a otra app, se corta solo y no gastamos
     * bateria ni perdemos memoria.
     */
    private fun observarDatos() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // La lista de servicios (siempre viene de Room)
                launch {
                    viewModel.servicios.collect { servicios ->
                        adaptador.submitList(servicios)
                        // Mostramos el mensaje de "no se encontraron servicios"
                        val vacia = servicios.isEmpty()
                        binding.estadoVacio.visibility = if (vacia) View.VISIBLE else View.GONE
                        binding.listaServicios.visibility = if (vacia) View.GONE else View.VISIBLE
                        binding.subtituloLista.visibility = if (vacia) View.GONE else View.VISIBLE
                    }
                }

                // Barrita de carga mientras se consulta la API.
                // INVISIBLE (no GONE) para que siempre ocupe su lugar y la lista
                // no se mueva de golpe cuando aparece o desaparece.
                launch {
                    viewModel.cargando.collect { cargando ->
                        binding.barraCarga.visibility =
                            if (cargando) View.VISIBLE else View.INVISIBLE
                    }
                }

                // Avisos: sin conexion o limite diario alcanzado
                launch {
                    viewModel.aviso.collect { idDelTexto ->
                        if (idDelTexto == null) {
                            binding.contenedorAviso.visibility = View.GONE
                        } else {
                            binding.contenedorAviso.visibility = View.VISIBLE
                            binding.textoAviso.setText(idDelTexto)
                        }
                    }
                }
            }
        }
    }
}
