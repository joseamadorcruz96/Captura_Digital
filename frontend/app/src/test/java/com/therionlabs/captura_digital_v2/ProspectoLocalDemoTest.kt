package com.therionlabs.captura_digital_v2

import com.therionlabs.captura_digital_v2.data.local.DemoSeedGenerator
import com.therionlabs.captura_digital_v2.data.model.AppMode
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.data.model.SyncStatus
import com.therionlabs.captura_digital_v2.data.model.dto.BatchSyncRequestDto
import com.therionlabs.captura_digital_v2.data.model.dto.BatchSyncResponseDto
import com.therionlabs.captura_digital_v2.data.model.dto.DashboardResumenDto
import com.therionlabs.captura_digital_v2.data.model.dto.EventoResponseDto
import com.therionlabs.captura_digital_v2.data.model.dto.ProspectoRequestDto
import com.therionlabs.captura_digital_v2.data.model.dto.ProspectoResponseDto
import com.therionlabs.captura_digital_v2.data.remote.ProspectoApi
import com.therionlabs.captura_digital_v2.data.repository.ProspectoRepository
import com.therionlabs.captura_digital_v2.util.RutUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProspectoLocalDemoTest {

    private class ThrowingProspectoApi : ProspectoApi {
        var callsCount = 0

        override suspend fun listar(captadorId: Int?, eventoId: String?, rut: String?, sede: String?): List<ProspectoResponseDto> {
            callsCount++
            error("No debe llamarse en LOCAL_DEMO")
        }

        override suspend fun obtener(clientUuid: String): ProspectoResponseDto {
            callsCount++
            error("No debe llamarse en LOCAL_DEMO")
        }

        override suspend fun crear(body: ProspectoRequestDto): ProspectoResponseDto {
            callsCount++
            error("No debe llamarse en LOCAL_DEMO")
        }

        override suspend fun actualizar(clientUuid: String, body: ProspectoRequestDto): ProspectoResponseDto {
            callsCount++
            error("No debe llamarse en LOCAL_DEMO")
        }

        override suspend fun eliminar(clientUuid: String) {
            callsCount++
            error("No debe llamarse en LOCAL_DEMO")
        }

        override suspend fun sincronizarLote(body: BatchSyncRequestDto): BatchSyncResponseDto {
            callsCount++
            error("No debe llamarse en LOCAL_DEMO")
        }

        override suspend fun obtenerResumen(eventoId: String?): DashboardResumenDto {
            callsCount++
            error("No debe llamarse en LOCAL_DEMO")
        }

        override suspend fun listarEventos(activo: Boolean?): List<EventoResponseDto> {
            callsCount++
            error("No debe llamarse en LOCAL_DEMO")
        }
    }

    @Test
    fun demoSeedGenerator_generaRutsValidosModulo11() {
        val seeds = DemoSeedGenerator.generarProspectosDemo()

        assertTrue(seeds.size >= 5)
        seeds.forEach { prospecto ->
            assertTrue("RUT inválido: ${prospecto.rut}", RutUtils.isValidRut(prospecto.rut))
        }
        assertTrue(seeds.any { it.syncStatus == SyncStatus.PENDING_SYNC })
        assertTrue(seeds.any { it.syncStatus == SyncStatus.SYNCED })
    }

    @Test
    fun localDemo_capturaSyncYCrudNoLlamanaApiRemota() = runTest {
        val throwingApi = ThrowingProspectoApi()
        val repo = ProspectoRepository(
            dao = null,
            api = throwingApi,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined),
            appModeProvider = { AppMode.LOCAL_DEMO }
        )

        val seeded = repo.poblarDatosDemo(resetear = true)
        assertTrue(seeded >= 5)

        val nuevo = Prospecto(
            id = "local-demo-nuevo",
            captadorId = 2,
            eventoId = DemoSeedGenerator.DEFAULT_EVENT_ID,
            rut = DemoSeedGenerator.validRut("19555666"),
            nombres = "Elena",
            apellidos = "Morales",
            comunaResidencia = "Viña del Mar",
            celular = "+56988776655",
            email = "elena@duoc.cl",
            situacionAcademica = SituacionAcademica.CUARTO_MEDIO,
            establecimientoEducacional = "Colegio Demo",
            comunaEstablecimiento = "Viña del Mar",
            jornadaPreferencia = Jornada.DIURNA,
            sedeInteres = "Viña del Mar",
            nivelInteres = NivelInteres.ALTO,
            perfilMotivacional = PerfilMotivacional.VOCACIONAL,
            carreraPrioridad1 = "Diseño Gráfico",
            syncStatus = SyncStatus.PENDING_SYNC
        )

        assertTrue(repo.guardarProspectoSuspend(nuevo))
        assertTrue(repo.refrescarDesdeServidor(2))
        assertNotNull(repo.obtenerEventoActivo())

        val syncNuevos = repo.sincronizarNuevosPendientes(captadorId = 2)
        assertTrue(syncNuevos.exitosos >= 1)
        assertFalse(syncNuevos.huboErrorRed)
        assertEquals(SyncStatus.SYNCED, repo.getAll().first { it.id == "local-demo-nuevo" }.syncStatus)

        val syncTotal = repo.sincronizarPendientes(captadorId = 2)
        assertEquals(0, syncTotal.fallidos)
        assertEquals(0, repo.getPendingSyncCount())

        assertTrue(repo.actualizarProspecto(nuevo.copy(nivelInteres = NivelInteres.MEDIO)))
        assertEquals(NivelInteres.MEDIO, repo.getAll().first { it.id == "local-demo-nuevo" }.nivelInteres)

        assertTrue(repo.eliminarProspecto("local-demo-nuevo"))
        assertTrue(repo.getAll().none { it.id == "local-demo-nuevo" })

        assertEquals(0, throwingApi.callsCount)
    }
}
