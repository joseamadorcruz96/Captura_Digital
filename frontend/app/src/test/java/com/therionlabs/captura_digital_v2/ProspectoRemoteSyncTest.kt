package com.therionlabs.captura_digital_v2

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ProspectoRemoteSyncTest {

    private class FakeProspectoApi(
        var failNetwork: Boolean = false,
        var failUuid: String? = null
    ) : ProspectoApi {
        val remoteStore = linkedMapOf<String, ProspectoResponseDto>()

        override suspend fun listar(
            captadorId: Int?,
            eventoId: String?,
            rut: String?,
            sede: String?
        ): List<ProspectoResponseDto> {
            if (failNetwork) throw IOException("Sin red")
            return remoteStore.values.toList()
        }

        override suspend fun obtener(clientUuid: String): ProspectoResponseDto =
            remoteStore[clientUuid] ?: error("No encontrado")

        override suspend fun crear(body: ProspectoRequestDto): ProspectoResponseDto =
            toResponse(body).also { remoteStore[body.clientUuid] = it }

        override suspend fun actualizar(clientUuid: String, body: ProspectoRequestDto): ProspectoResponseDto {
            if (failNetwork) throw IOException("Sin red")
            return toResponse(body).also { remoteStore[clientUuid] = it }
        }

        override suspend fun eliminar(clientUuid: String) {
            if (failNetwork) throw IOException("Sin red")
            remoteStore.remove(clientUuid)
        }

        override suspend fun sincronizarLote(body: BatchSyncRequestDto): BatchSyncResponseDto {
            if (failNetwork) throw IOException("Sin red")
            val exitosos = mutableListOf<String>()
            val fallidos = mutableListOf<String>()
            body.prospectos.forEach { item ->
                if (item.clientUuid == failUuid) {
                    fallidos.add(item.clientUuid)
                } else {
                    exitosos.add(item.clientUuid)
                    remoteStore[item.clientUuid] = toResponse(item)
                }
            }
            return BatchSyncResponseDto(
                totalProcesados = body.prospectos.size,
                exitososUuids = exitosos,
                fallidosUuids = fallidos,
                mensaje = "Sync simulado: ${exitosos.size} ok, ${fallidos.size} error"
            )
        }

        override suspend fun obtenerResumen(eventoId: String?) = DashboardResumenDto(0L)
        override suspend fun listarEventos(activo: Boolean?) = emptyList<EventoResponseDto>()

        private fun toResponse(req: ProspectoRequestDto) = ProspectoResponseDto(
            clientUuid = req.clientUuid,
            captadorId = req.captadorId,
            eventoId = req.eventoId,
            timestampEpoch = req.timestampEpoch,
            rut = req.rut,
            nombres = req.nombres,
            apellidos = req.apellidos,
            genero = req.genero,
            fechaNacimiento = req.fechaNacimiento,
            comunaResidencia = req.comunaResidencia,
            celular = req.celular,
            email = req.email,
            consentimientoDatos = req.consentimientoDatos,
            situacionAcademica = req.situacionAcademica,
            situacionAcademicaOtro = req.situacionAcademicaOtro,
            establecimientoEducacional = req.establecimientoEducacional,
            comunaEstablecimiento = req.comunaEstablecimiento,
            jornadaPreferencia = req.jornadaPreferencia,
            sedeInteres = req.sedeInteres,
            nivelInteres = req.nivelInteres,
            perfilMotivacional = req.perfilMotivacional,
            carreras = req.carreras
        )
    }

    private fun sampleProspecto(uuid: String, rut: String) = Prospecto(
        id = uuid,
        captadorId = 2,
        eventoId = "EVT-TEST",
        rut = rut,
        nombres = "Sofía",
        apellidos = "Molina",
        comunaResidencia = "Viña del Mar",
        celular = "+56912345678",
        email = "$uuid@duoc.cl",
        situacionAcademica = SituacionAcademica.CUARTO_MEDIO,
        establecimientoEducacional = "Liceo A",
        comunaEstablecimiento = "Viña del Mar",
        jornadaPreferencia = Jornada.DIURNA,
        sedeInteres = "Viña del Mar",
        nivelInteres = NivelInteres.ALTO,
        perfilMotivacional = PerfilMotivacional.VOCACIONAL,
        carreraPrioridad1 = "Diseño Gráfico",
        syncStatus = SyncStatus.PENDING_SYNC
    )

    @Test
    fun sincronizarPendientes_marcaExitososComoSyncedYFallidosComoSyncError() = runTest {
        val fakeApi = FakeProspectoApi(failUuid = "uuid-malo")
        val repo = ProspectoRepository(dao = null, api = fakeApi, coroutineScope = CoroutineScope(Dispatchers.Unconfined))

        repo.guardarProspecto(sampleProspecto("uuid-ok", "11.111.111-1"))
        repo.guardarProspecto(sampleProspecto("uuid-malo", "11.111.112-K"))

        val result = repo.sincronizarPendientes(captadorId = 2, eventoId = "EVT-TEST")

        assertTrue(result.exitosos >= 1)
        assertEquals(1, result.fallidos)
        val okItem = repo.getAll().first { it.id == "uuid-ok" }
        val badItem = repo.getAll().first { it.id == "uuid-malo" }
        assertEquals(SyncStatus.SYNCED, okItem.syncStatus)
        assertEquals(SyncStatus.SYNC_ERROR, badItem.syncStatus)
    }

    @Test
    fun sincronizarPendientes_sinRedMantieneRegistrosPendientes() = runTest {
        val fakeApi = FakeProspectoApi(failNetwork = true)
        val repo = ProspectoRepository(dao = null, api = fakeApi, coroutineScope = CoroutineScope(Dispatchers.Unconfined))
        repo.guardarProspecto(sampleProspecto("uuid-offline", "11.111.117-0"))

        val result = repo.sincronizarPendientes(captadorId = 2)

        assertEquals(0, result.exitosos)
        assertTrue(result.mensaje.contains("Sin conexión"))
        assertEquals(SyncStatus.PENDING_SYNC, repo.getAll().first { it.id == "uuid-offline" }.syncStatus)
    }

    @Test
    fun actualizarYEliminar_funcionanContraApiYFlujoLocal() = runTest {
        val fakeApi = FakeProspectoApi()
        val repo = ProspectoRepository(dao = null, api = fakeApi, coroutineScope = CoroutineScope(Dispatchers.Unconfined))
        val item = sampleProspecto("uuid-crud", "12.345.678-5")
        repo.guardarProspecto(item)

        repo.actualizarProspecto(item.copy(nivelInteres = NivelInteres.BAJO))
        assertEquals(NivelInteres.BAJO, repo.getAll().first { it.id == "uuid-crud" }.nivelInteres)
        assertEquals(SyncStatus.SYNCED, repo.getAll().first { it.id == "uuid-crud" }.syncStatus)

        repo.eliminarProspecto("uuid-crud")
        assertTrue(repo.getAll().none { it.id == "uuid-crud" })
    }
}
