package com.therionlabs.captura_digital_v2

import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.data.model.SyncStatus
import com.therionlabs.captura_digital_v2.data.repository.AuthRepository
import com.therionlabs.captura_digital_v2.data.repository.IProspectoRepository
import com.therionlabs.captura_digital_v2.data.repository.SyncResult
import com.therionlabs.captura_digital_v2.ui.inicio.InicioViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class InicioViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeSyncRepo : IProspectoRepository {
        private val items = MutableStateFlow(
            listOf(
                Prospecto(
                    id = "u1", captadorId = 2, rut = "11.111.111-1",
                    nombres = "Ana", apellidos = "López", comunaResidencia = "Viña",
                    celular = "+56911111111", email = "ana@test.cl",
                    situacionAcademica = SituacionAcademica.CUARTO_MEDIO,
                    establecimientoEducacional = "Colegio", comunaEstablecimiento = "Viña",
                    jornadaPreferencia = Jornada.DIURNA, sedeInteres = "Viña",
                    nivelInteres = NivelInteres.ALTO, perfilMotivacional = PerfilMotivacional.INVERSION,
                    carreraPrioridad1 = "Auditoría", syncStatus = SyncStatus.PENDING_SYNC
                )
            )
        )

        override fun guardarProspecto(prospecto: Prospecto) = true
        override fun getAll(): List<Prospecto> = items.value
        override fun getByCaptador(captadorId: Int) = items.value.filter { it.captadorId == captadorId }
        override fun getPendingSyncCount() = items.value.count { it.syncStatus != SyncStatus.SYNCED }
        override fun observeProspectos(): Flow<List<Prospecto>> = items

        override suspend fun sincronizarPendientes(captadorId: Int, eventoId: String?): SyncResult {
            items.value = items.value.map { it.copy(syncStatus = SyncStatus.SYNCED) }
            return SyncResult(1, 1, 0, "Sincronización completada exitosamente.")
        }
    }

    @Test
    fun sincronizarAhora_actualizaPendientesYMensaje() = runTest {
        val viewModel = InicioViewModel(AuthRepository(FakeAuthApi()), FakeSyncRepo())
        assertEquals(1, viewModel.uiState.value.pendingSyncCount)

        viewModel.sincronizarAhora()

        assertFalse(viewModel.uiState.value.isSyncing)
        assertEquals(0, viewModel.uiState.value.pendingSyncCount)
        assertEquals("Sincronización completada exitosamente.", viewModel.uiState.value.syncMessage)
    }
}
