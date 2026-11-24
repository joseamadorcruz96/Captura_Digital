package com.therionlabs.captura_digital_v2.ui.eventos

import com.therionlabs.captura_digital_v2.data.local.DemoSeedGenerator
import com.therionlabs.captura_digital_v2.data.model.Evento
import com.therionlabs.captura_digital_v2.data.model.TipoEvento

enum class EventoFilter(val displayName: String) {
    ACTIVOS("Activos"),
    TODOS("Todos"),
    ARCHIVADOS("Archivados")
}

data class EventosUiState(
    val eventos: List<Evento> = emptyList(),
    val filtro: EventoFilter = EventoFilter.ACTIVOS,
    val activeEventId: String = DemoSeedGenerator.DEFAULT_EVENT_ID,
    val mostrarFormulario: Boolean = false,
    val editandoId: String? = null,
    val nombre: String = "",
    val tipo: TipoEvento = TipoEvento.FERIA,
    val sedeAsociada: String = "Viña del Mar",
    val ubicacion: String = "",
    val fechaInicioText: String = "",
    val fechaFinText: String = "",
    val metaText: String = "30",
    val nombreError: String? = null,
    val sedeError: String? = null,
    val ubicacionError: String? = null,
    val fechaInicioError: String? = null,
    val fechaFinError: String? = null,
    val metaError: String? = null,
    val cargando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
) {
    val eventosFiltrados: List<Evento>
        get() = when (filtro) {
            EventoFilter.ACTIVOS -> eventos.filter { it.activo && !it.archivado }
            EventoFilter.TODOS -> eventos
            EventoFilter.ARCHIVADOS -> eventos.filter { it.archivado }
        }
}
