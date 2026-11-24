package com.therionlabs.captura_digital_v2.ui.eventos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.therionlabs.captura_digital_v2.data.model.Evento
import com.therionlabs.captura_digital_v2.data.model.TipoEvento
import com.therionlabs.captura_digital_v2.data.repository.EventoRepository
import com.therionlabs.captura_digital_v2.data.repository.IEventoRepository
import com.therionlabs.captura_digital_v2.util.EventoDateUtils
import com.therionlabs.captura_digital_v2.util.EventoValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel para la pantalla de Administración de Eventos / Ferias (solo [UserRole.ADMINISTRATIVO]).
 */
class EventosViewModel(
    private val eventoRepository: IEventoRepository = EventoRepository(),
    private val nowProvider: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventosUiState())
    val uiState: StateFlow<EventosUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            eventoRepository.observeEventos().collect { lista ->
                _uiState.update { it.copy(eventos = lista) }
            }
        }
        viewModelScope.launch {
            eventoRepository.observeEventoActivo().collect { activo ->
                if (activo != null) {
                    _uiState.update { it.copy(activeEventId = activo.id) }
                }
            }
        }
    }

    fun onFiltroChange(filtro: EventoFilter) {
        _uiState.update { it.copy(filtro = filtro) }
    }

    fun abrirNuevoEvento() {
        val hoy = nowProvider()
        val manana = hoy + 24 * 60 * 60 * 1000L
        _uiState.update {
            it.copy(
                mostrarFormulario = true,
                editandoId = null,
                nombre = "",
                tipo = TipoEvento.FERIA,
                sedeAsociada = "Viña del Mar",
                ubicacion = "",
                fechaInicioText = EventoDateUtils.formatDisplayDate(hoy),
                fechaFinText = EventoDateUtils.formatDisplayDate(manana),
                metaText = "30",
                nombreError = null,
                sedeError = null,
                ubicacionError = null,
                fechaInicioError = null,
                fechaFinError = null,
                metaError = null,
                error = null
            )
        }
    }

    fun editarEvento(evento: Evento) {
        _uiState.update {
            it.copy(
                mostrarFormulario = true,
                editandoId = evento.id,
                nombre = evento.nombre,
                tipo = evento.tipo,
                sedeAsociada = evento.sedeAsociada,
                ubicacion = evento.ubicacion,
                fechaInicioText = EventoDateUtils.formatDisplayDate(evento.fechaInicio),
                fechaFinText = EventoDateUtils.formatDisplayDate(evento.fechaFin),
                metaText = evento.metaProspectos.toString(),
                nombreError = null,
                sedeError = null,
                ubicacionError = null,
                fechaInicioError = null,
                fechaFinError = null,
                metaError = null,
                error = null
            )
        }
    }

    fun cerrarFormulario() {
        _uiState.update {
            it.copy(
                mostrarFormulario = false,
                editandoId = null,
                error = null
            )
        }
    }

    fun onNombreChange(value: String) {
        _uiState.update { it.copy(nombre = value, nombreError = null, error = null) }
    }

    fun onTipoChange(value: TipoEvento) {
        _uiState.update { it.copy(tipo = value, error = null) }
    }

    fun onSedeChange(value: String) {
        _uiState.update { it.copy(sedeAsociada = value, sedeError = null, error = null) }
    }

    fun onUbicacionChange(value: String) {
        _uiState.update { it.copy(ubicacion = value, ubicacionError = null, error = null) }
    }

    fun onFechaInicioChange(value: String) {
        _uiState.update { it.copy(fechaInicioText = value, fechaInicioError = null, fechaFinError = null, error = null) }
    }

    fun onFechaFinChange(value: String) {
        _uiState.update { it.copy(fechaFinText = value, fechaFinError = null, error = null) }
    }

    fun onMetaChange(value: String) {
        _uiState.update { it.copy(metaText = value.filter(Char::isDigit), metaError = null, error = null) }
    }

    fun guardarEvento() {
        val state = _uiState.value
        val nombreErr = EventoValidator.validarNombre(state.nombre).errorOrNull()
        val sedeErr = EventoValidator.validarSede(state.sedeAsociada).errorOrNull()
        val ubicacionErr = EventoValidator.validarUbicacion(state.ubicacion).errorOrNull()
        val metaErr = EventoValidator.validarMeta(state.metaText).errorOrNull()
        val fechaInicioErr = EventoValidator.validarFecha(state.fechaInicioText, "fecha de inicio").errorOrNull()
        var fechaFinErr = EventoValidator.validarFecha(state.fechaFinText, "fecha de término").errorOrNull()

        if (fechaInicioErr == null && fechaFinErr == null) {
            fechaFinErr = EventoValidator.validarRangoFechas(state.fechaInicioText, state.fechaFinText).errorOrNull()
        }

        if (listOf(nombreErr, sedeErr, ubicacionErr, metaErr, fechaInicioErr, fechaFinErr).any { it != null }) {
            _uiState.update {
                it.copy(
                    nombreError = nombreErr,
                    sedeError = sedeErr,
                    ubicacionError = ubicacionErr,
                    metaError = metaErr,
                    fechaInicioError = fechaInicioErr,
                    fechaFinError = fechaFinErr
                )
            }
            return
        }

        val inicioEpoch = EventoDateUtils.parseDate(state.fechaInicioText, endOfDay = false) ?: return
        val finEpoch = EventoDateUtils.parseDate(state.fechaFinText, endOfDay = true) ?: return
        val meta = state.metaText.trim().toInt()
        val existente = state.editandoId?.let { id -> state.eventos.firstOrNull { it.id == id } }

        val evento = Evento(
            id = existente?.id ?: "EVT-${UUID.randomUUID().toString().take(8).uppercase()}",
            nombre = state.nombre.trim(),
            tipo = state.tipo,
            sedeAsociada = state.sedeAsociada.trim(),
            ubicacion = state.ubicacion.trim(),
            fechaInicio = inicioEpoch,
            fechaFin = finEpoch,
            metaProspectos = meta,
            activo = existente?.activo ?: true,
            archivado = existente?.archivado ?: false
        )

        _uiState.update { it.copy(cargando = true) }
        viewModelScope.launch {
            val ok = eventoRepository.guardarEvento(evento)
            _uiState.update {
                it.copy(
                    cargando = false,
                    mostrarFormulario = !ok,
                    editandoId = if (ok) null else it.editandoId,
                    mensaje = if (ok) {
                        if (existente != null) "Evento actualizado correctamente." else "Evento creado correctamente."
                    } else null,
                    error = if (!ok) "No se pudo guardar el evento." else null
                )
            }
        }
    }

    fun toggleActivo(evento: Evento) {
        if (evento.archivado) return
        viewModelScope.launch {
            val nuevoEstado = !evento.activo
            val ok = eventoRepository.cambiarEstadoActivo(evento.id, nuevoEstado)
            if (ok) {
                _uiState.update {
                    it.copy(mensaje = if (nuevoEstado) "Evento activado: ${evento.nombre}" else "Evento desactivado: ${evento.nombre}")
                }
            }
        }
    }

    fun toggleArchivar(evento: Evento) {
        viewModelScope.launch {
            val nuevoArchivado = !evento.archivado
            val ok = eventoRepository.archivarEvento(evento.id, nuevoArchivado)
            if (ok) {
                _uiState.update {
                    it.copy(mensaje = if (nuevoArchivado) "Evento archivado: ${evento.nombre}" else "Evento restaurado: ${evento.nombre}")
                }
            }
        }
    }

    fun seleccionarComoActivo(evento: Evento) {
        if (!evento.estaDisponibleParaCaptura) return
        viewModelScope.launch {
            val selected = eventoRepository.seleccionarEventoActivo(evento.id)
            if (selected != null) {
                _uiState.update {
                    it.copy(
                        activeEventId = selected.id,
                        mensaje = "Evento activo cambiado a ${selected.nombre}"
                    )
                }
            }
        }
    }
}
