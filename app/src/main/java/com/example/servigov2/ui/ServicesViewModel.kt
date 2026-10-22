package com.example.servigov2.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.servigov2.R
import com.example.servigov2.data.ResultadoActualizacion
import com.example.servigov2.data.ServiceEntity
import com.example.servigov2.data.ServiceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * El ViewModel guarda los datos de la pantalla y sobrevive a los giros del telefono.
 * La Activity solo se encarga de dibujar; la logica vive aca.
 *
 * Usamos AndroidViewModel (en vez de ViewModel) porque necesitamos el Context
 * para crear el repositorio, y asi nos ahorramos escribir una Factory.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class ServicesViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = ServiceRepository(application)

    /** Lo que el usuario escribio en el buscador. */
    val textoBusqueda = MutableStateFlow("")

    /** true mientras se esta consultando la API (para mostrar la barrita de carga). */
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    /** Id del texto de strings.xml que hay que avisar, o null si no hay nada que avisar. */
    private val _aviso = MutableStateFlow<Int?>(null)
    val aviso: StateFlow<Int?> = _aviso

    /**
     * La lista que ve el usuario. SIEMPRE sale de Room, nunca de internet.
     *
     * flatMapLatest: cada vez que cambia el texto del buscador, cancela la consulta
     * anterior y arranca la nueva. Si el texto esta vacio mostramos todo lo guardado;
     * si no, filtramos con LIKE dentro de la base de datos.
     */
    val servicios: StateFlow<List<ServiceEntity>> = textoBusqueda
        .flatMapLatest { texto ->
            if (texto.isBlank()) {
                repositorio.obtenerTodos()
            } else {
                repositorio.buscarGuardados(texto)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // 1) Primera carga: si la base esta vacia traemos servicios de la API.
        //    Si ya hay datos guardados no gastamos una llamada al pedo.
        viewModelScope.launch {
            if (!repositorio.hayServiciosGuardados()) {
                actualizarDesdeApi(ServiceRepository.CONSULTA_INICIAL)
            }
        }

        // 2) Busqueda con debounce.
        //    debounce(400) espera 400 milisegundos de "silencio" antes de llamar a la
        //    API. Si el usuario escribe "plomero" no hacemos 7 pedidos, hacemos 1.
        viewModelScope.launch {
            textoBusqueda
                .debounce(400)
                .filter { it.isNotBlank() }      // la API rechaza las busquedas vacias
                .distinctUntilChanged()          // si el texto no cambio, no repetimos
                .collect { texto -> actualizarDesdeApi(texto) }
        }
    }

    /** La Activity llama a esto cada vez que el usuario escribe una letra. */
    fun alCambiarTexto(texto: String) {
        textoBusqueda.value = texto
    }

    /** Llama a la API y guarda en Room. La lista se actualiza sola por el Flow. */
    private suspend fun actualizarDesdeApi(consulta: String) {
        _cargando.value = true
        val resultado = repositorio.actualizarDesdeApi(consulta)
        _cargando.value = false

        _aviso.value = when (resultado) {
            ResultadoActualizacion.LIMITE_ALCANZADO -> R.string.aviso_limite_diario
            ResultadoActualizacion.SIN_CONEXION -> R.string.aviso_sin_conexion
            ResultadoActualizacion.OK -> null
        }
    }
}
