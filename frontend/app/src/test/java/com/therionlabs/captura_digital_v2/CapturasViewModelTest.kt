package com.therionlabs.captura_digital_v2

import com.therionlabs.captura_digital_v2.data.local.InMemorySessionStorage
import com.therionlabs.captura_digital_v2.data.local.LocalUserDataSource
import com.therionlabs.captura_digital_v2.data.model.AppMode
import com.therionlabs.captura_digital_v2.data.model.EstadoCompletitud
import com.therionlabs.captura_digital_v2.data.model.FormularioBorrador
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.data.model.SyncStatus
import com.therionlabs.captura_digital_v2.data.repository.AuthRepository
import com.therionlabs.captura_digital_v2.data.repository.BorradorRepository
import com.therionlabs.captura_digital_v2.data.repository.ProspectoRepository
import com.therionlabs.captura_digital_v2.ui.capturas.CapturasFilter
import com.therionlabs.captura_digital_v2.ui.capturas.CapturasViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CapturasViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun consolidado_distingueBorradoresNoSincronizablesYCapturasPendientesOSincronizadas() = runTest {
        val storage = InMemorySessionStorage(
            defaultAppMode = AppMode.LOCAL_DEMO,
            initialUser = LocalUserDataSource.DEMO_CAPTADOR
        )
        val authRepo = AuthRepository(
            api = FakeAuthApi(),
            localDataSource = LocalUserDataSource(),
            sessionStorage = storage,
            scope = CoroutineScope(Dispatchers.Unconfined)
        )
        val prospectoRepo = ProspectoRepository(
            dao = null,
            api = null,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined),
            appModeProvider = { AppMode.LOCAL_DEMO }
        )
        val borradorRepo = BorradorRepository(dao = null)

        val draft = FormularioBorrador(
            id = "draft-captador-1",
            captadorId = LocalUserDataSource.DEMO_CAPTADOR.id,
            nombres = "Valentina",
            apellidos = "Borrador"
        )
        assertTrue(borradorRepo.guardarBorrador(draft))

        val expressPending = Prospecto(
            id = "exp-pending-1",
            captadorId = LocalUserDataSource.DEMO_CAPTADOR.id,
            rut = "11.111.111-1",
            nombres = "Diego",
            apellidos = "Express",
            comunaResidencia = "Por completar",
            celular = "+56912345678",
            email = "",
            situacionAcademica = SituacionAcademica.CUARTO_MEDIO,
            establecimientoEducacional = "Por completar",
            comunaEstablecimiento = "Por completar",
            jornadaPreferencia = Jornada.DIURNA,
            sedeInteres = "Viña del Mar",
            nivelInteres = NivelInteres.MEDIO,
            perfilMotivacional = PerfilMotivacional.VOCACIONAL,
            carreraPrioridad1 = "Ingeniería en Informática",
            estadoCompletitud = EstadoCompletitud.EXPRESS,
            syncStatus = SyncStatus.PENDING_SYNC
        )
        prospectoRepo.guardarProspectoSuspend(expressPending)

        val vm = CapturasViewModel(
            authRepository = authRepo,
            prospectoRepository = prospectoRepo,
            borradorRepository = borradorRepo
        )

        assertEquals(1, vm.uiState.value.borradores.size)
        assertTrue(vm.uiState.value.capturas.any { it.id == "exp-pending-1" })

        // Filtro BORRADORES oculta capturas
        vm.onFilterSelected(CapturasFilter.BORRADORES)
        assertEquals(1, vm.uiState.value.borradoresFiltrados.size)
        assertTrue(vm.uiState.value.capturasFiltradas.isEmpty())

        // Sincronizar pendientes deja el borrador intacto (no sincronizable) y marca la captura como SYNCED
        vm.onFilterSelected(CapturasFilter.TODOS)
        vm.sincronizarPendientes()
        assertEquals(1, borradorRepo.observeByCaptador(LocalUserDataSource.DEMO_CAPTADOR.id).first().size)
        assertEquals(
            SyncStatus.SYNCED,
            vm.uiState.value.capturas.first { it.id == "exp-pending-1" }.syncStatus
        )

        // Descartar borrador lo elimina del listado
        vm.descartarBorrador(draft)
        assertTrue(vm.uiState.value.borradores.isEmpty())
    }
}
