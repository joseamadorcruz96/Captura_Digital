package com.therionlabs.captura_digital_v2

import com.therionlabs.captura_digital_v2.data.local.DemoSeedGenerator
import com.therionlabs.captura_digital_v2.data.local.InMemorySessionStorage
import com.therionlabs.captura_digital_v2.data.model.AppMode
import com.therionlabs.captura_digital_v2.data.model.TipoEvento
import com.therionlabs.captura_digital_v2.data.repository.EventoRepository
import com.therionlabs.captura_digital_v2.ui.eventos.EventoFilter
import com.therionlabs.captura_digital_v2.ui.eventos.EventosViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EventosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun crearViewModel(): EventosViewModel {
        val repo = EventoRepository(
            eventoDao = null,
            turnoDao = null,
            api = null,
            sessionStorage = InMemorySessionStorage(defaultAppMode = AppMode.LOCAL_DEMO),
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )
        return EventosViewModel(eventoRepository = repo)
    }

    @Test
    fun filtros_activosTodosYArchivadosFiltranCorrectamente() {
        val vm = crearViewModel()

        val activos = vm.uiState.value.eventosFiltrados
        assertTrue(activos.isNotEmpty())
        assertTrue(activos.all { it.activo && !it.archivado })

        vm.onFiltroChange(EventoFilter.TODOS)
        val todos = vm.uiState.value.eventosFiltrados
        assertEquals(DemoSeedGenerator.generarEventosDemo().size, todos.size)

        vm.onFiltroChange(EventoFilter.ARCHIVADOS)
        val archivados = vm.uiState.value.eventosFiltrados
        assertTrue(archivados.isNotEmpty())
        assertTrue(archivados.all { it.archivado })
    }

    @Test
    fun guardarEvento_validaNombreMetaYFechasInvalidas() {
        val vm = crearViewModel()
        vm.abrirNuevoEvento()

        vm.onNombreChange("  ")
        vm.onUbicacionChange("")
        vm.onMetaChange("0")
        vm.onFechaInicioChange("20/11/2026")
        vm.onFechaFinChange("18/11/2026")
        vm.guardarEvento()

        val state = vm.uiState.value
        assertTrue(state.mostrarFormulario)
        assertNotNull(state.nombreError)
        assertNotNull(state.ubicacionError)
        assertNotNull(state.metaError)
        assertNotNull(state.fechaFinError)
    }

    @Test
    fun crearYEditarEvento_persisteYCambiaEstadoActivo() = runTest {
        val vm = crearViewModel()
        vm.abrirNuevoEvento()

        vm.onNombreChange("Feria Colegio Alemán")
        vm.onTipoChange(TipoEvento.COLEGIO)
        vm.onSedeChange("Valparaíso")
        vm.onUbicacionChange("Colegio Alemán, Valparaíso")
        vm.onFechaInicioChange("25/11/2026")
        vm.onFechaFinChange("26/11/2026")
        vm.onMetaChange("45")
        vm.guardarEvento()

        assertFalse(vm.uiState.value.mostrarFormulario)
        assertNull(vm.uiState.value.error)

        val creado = vm.uiState.value.eventos.firstOrNull { it.nombre == "Feria Colegio Alemán" }
        assertNotNull(creado)
        assertEquals(45, creado?.metaProspectos)
        assertEquals(TipoEvento.COLEGIO, creado?.tipo)

        // Editar evento existente
        vm.editarEvento(creado!!)
        vm.onMetaChange("60")
        vm.guardarEvento()

        val editado = vm.uiState.value.eventos.first { it.id == creado.id }
        assertEquals(60, editado.metaProspectos)

        // Seleccionar como evento activo y luego desactivar
        vm.seleccionarComoActivo(editado)
        assertEquals(editado.id, vm.uiState.value.activeEventId)

        vm.toggleActivo(editado)
        val desactivado = vm.uiState.value.eventos.first { it.id == editado.id }
        assertFalse(desactivado.activo)
    }
}
