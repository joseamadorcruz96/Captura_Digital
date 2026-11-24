package com.therionlabs.captura_digital_v2

import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.PreferencesSerializer
import com.therionlabs.captura_digital_v2.data.local.DataStoreSessionStorage
import com.therionlabs.captura_digital_v2.data.local.DemoSeedGenerator
import com.therionlabs.captura_digital_v2.data.local.InMemorySessionStorage
import com.therionlabs.captura_digital_v2.data.model.AppMode
import com.therionlabs.captura_digital_v2.data.model.Evento
import com.therionlabs.captura_digital_v2.data.model.TipoEvento
import com.therionlabs.captura_digital_v2.data.model.dto.BatchSyncRequestDto
import com.therionlabs.captura_digital_v2.data.model.dto.BatchSyncResponseDto
import com.therionlabs.captura_digital_v2.data.model.dto.DashboardResumenDto
import com.therionlabs.captura_digital_v2.data.model.dto.EventoResponseDto
import com.therionlabs.captura_digital_v2.data.model.dto.ProspectoRequestDto
import com.therionlabs.captura_digital_v2.data.model.dto.ProspectoResponseDto
import com.therionlabs.captura_digital_v2.data.remote.ProspectoApi
import com.therionlabs.captura_digital_v2.data.repository.EventoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path.Companion.toPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class EventoRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private class FakeEventosApi(
        private val eventos: List<EventoResponseDto> = emptyList()
    ) : ProspectoApi {
        var listarEventosCalls = 0

        override suspend fun listar(captadorId: Int?, eventoId: String?, rut: String?, sede: String?) = emptyList<ProspectoResponseDto>()
        override suspend fun obtener(clientUuid: String) = error("No usado")
        override suspend fun crear(body: ProspectoRequestDto) = error("No usado")
        override suspend fun actualizar(clientUuid: String, body: ProspectoRequestDto) = error("No usado")
        override suspend fun eliminar(clientUuid: String) {}
        override suspend fun sincronizarLote(body: BatchSyncRequestDto) = BatchSyncResponseDto(0, emptyList(), emptyList(), "")
        override suspend fun obtenerResumen(eventoId: String?) = DashboardResumenDto(0L)
        override suspend fun listarEventos(activo: Boolean?): List<EventoResponseDto> {
            listarEventosCalls++
            return if (activo == null) eventos else eventos.filter { it.activo == activo }
        }
    }

    @Test
    fun localDemo_resuelveEventosSeedYNoLlamaApiRemota() = runTest {
        val fakeApi = FakeEventosApi()
        val storage = InMemorySessionStorage(defaultAppMode = AppMode.LOCAL_DEMO)
        val repo = EventoRepository(
            eventoDao = null,
            turnoDao = null,
            api = fakeApi,
            sessionStorage = storage,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )

        assertTrue(repo.refrescarEventosDesdeServidor())
        assertEquals(0, fakeApi.listarEventosCalls)

        val activo = repo.getEventoActivo()
        assertNotNull(activo)
        assertEquals(DemoSeedGenerator.DEFAULT_EVENT_ID, activo?.id)
    }

    @Test
    fun dataStore_persisteEventoActivoSeleccionadoEntreReinicios() {
        runBlocking {
        val prefsFile = File(tempFolder.newFolder(), "evento_session_test.preferences_pb")

        val scope1 = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val ds1 = PreferenceDataStoreFactory.create(
            storage = OkioStorage(FileSystem.SYSTEM, PreferencesSerializer) { prefsFile.absolutePath.toPath() },
            scope = scope1
        )
        val storage1 = DataStoreSessionStorage(ds1, defaultAppMode = AppMode.LOCAL_DEMO, scope = scope1)
        storage1.awaitInitialLoad()

        val repo1 = EventoRepository(
            eventoDao = null,
            turnoDao = null,
            api = null,
            sessionStorage = storage1,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )
        val selected = repo1.seleccionarEventoActivo("EVT-COL-RUBEN-2026")
        assertNotNull(selected)
        assertEquals("EVT-COL-RUBEN-2026", storage1.currentActiveEventId)
        scope1.coroutineContext[Job]?.cancelAndJoin()

        // Segundo arranque simulado leyendo desde el mismo archivo DataStore
        val scope2 = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val ds2 = PreferenceDataStoreFactory.create(
            storage = OkioStorage(FileSystem.SYSTEM, PreferencesSerializer) { prefsFile.absolutePath.toPath() },
            scope = scope2
        )
        val storage2 = DataStoreSessionStorage(ds2, defaultAppMode = AppMode.LOCAL_DEMO, scope = scope2)
        storage2.awaitInitialLoad()

        val repo2 = EventoRepository(
            eventoDao = null,
            turnoDao = null,
            api = null,
            sessionStorage = storage2,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )
        val restored = repo2.getEventoActivo()
        assertEquals("EVT-COL-RUBEN-2026", restored?.id)
        assertEquals("Charla Vocacional Colegio Rubén Castro", restored?.nombre)
        scope2.coroutineContext[Job]?.cancelAndJoin()
        }
    }

    @Test
    fun remoteGateway_refrescaEventosDesdeApiYMapeaCampos() = runTest {
        val fakeApi = FakeEventosApi(
            eventos = listOf(
                EventoResponseDto(
                    id = "EVT-REM-01",
                    nombre = "Feria Regional Valparaíso",
                    establecimiento = "Liceo A-23",
                    comuna = "Valparaíso",
                    sedeResponsable = "Valparaíso",
                    fechaEvento = "2026-11-25",
                    activo = true,
                    metaProspectos = 85
                )
            )
        )
        val storage = InMemorySessionStorage(defaultAppMode = AppMode.REMOTE_GATEWAY)
        val repo = EventoRepository(
            eventoDao = null,
            turnoDao = null,
            api = fakeApi,
            sessionStorage = storage,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )

        assertTrue(repo.refrescarEventosDesdeServidor())
        assertTrue(fakeApi.listarEventosCalls >= 1)

        val eventos = repo.observeEventos().first()
        val remoto = eventos.firstOrNull { it.id == "EVT-REM-01" }
        assertNotNull(remoto)
        assertEquals("Valparaíso", remoto?.sedeAsociada)
        assertEquals(85, remoto?.metaProspectos)
        assertEquals("Liceo A-23, Valparaíso", remoto?.ubicacion)
    }

    @Test
    fun adminCrud_crearDesactivarYArchivarRevalidaEventoActivo() = runTest {
        val storage = InMemorySessionStorage(defaultAppMode = AppMode.LOCAL_DEMO)
        val repo = EventoRepository(
            eventoDao = null,
            turnoDao = null,
            api = null,
            sessionStorage = storage,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )

        val nuevo = Evento(
            id = "EVT-CUSTOM-1",
            nombre = "Casa Abierta Maipú",
            tipo = TipoEvento.CASA_ABIERTA,
            sedeAsociada = "Maipú",
            ubicacion = "Av. Esquina Blanca 501, Maipú",
            fechaInicio = 1776540000000L,
            fechaFin = 1776626400000L,
            metaProspectos = 50,
            activo = true,
            archivado = false
        )
        assertTrue(repo.guardarEvento(nuevo))
        assertNotNull(repo.seleccionarEventoActivo("EVT-CUSTOM-1"))
        assertEquals("EVT-CUSTOM-1", repo.getEventoActivoId())

        // Si se archiva el evento activo en uso, el repositorio debe revalidar hacia otro disponible
        assertTrue(repo.archivarEvento("EVT-CUSTOM-1", true))
        val revalidado = repo.getEventoActivo()
        assertNotNull(revalidado)
        assertNotEquals("EVT-CUSTOM-1", revalidado?.id)

        val archivado = repo.observeEventos().first().first { it.id == "EVT-CUSTOM-1" }
        assertTrue(archivado.archivado)
        assertFalse(archivado.activo)

        // Al desarchivar sigue inactivo hasta reactivarlo explícitamente
        assertTrue(repo.archivarEvento("EVT-CUSTOM-1", false))
        assertTrue(repo.cambiarEstadoActivo("EVT-CUSTOM-1", true))
        val reactivado = repo.observeEventos().first().first { it.id == "EVT-CUSTOM-1" }
        assertFalse(reactivado.archivado)
        assertTrue(reactivado.activo)
    }

    @Test
    fun turnos_iniciarYFinalizarMantieneUnTurnoActivoPorCaptador() = runTest {
        var fakeTime = 1_000_000L
        val storage = InMemorySessionStorage(defaultAppMode = AppMode.LOCAL_DEMO)
        val repo = EventoRepository(
            eventoDao = null,
            turnoDao = null,
            api = null,
            sessionStorage = storage,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined),
            nowProvider = { fakeTime }
        )

        val t1 = repo.iniciarTurno(captadorId = 2, eventoId = DemoSeedGenerator.DEFAULT_EVENT_ID, metaTurno = 20)
        assertTrue(t1.estaActivo)
        assertEquals(t1.id, repo.observeTurnoActivo(2).first()?.id)

        // Iniciar un segundo turno para el mismo captador cierra el anterior
        fakeTime = 2_000_000L
        val t2 = repo.iniciarTurno(captadorId = 2, eventoId = "EVT-COL-RUBEN-2026", metaTurno = 15)
        assertEquals(t2.id, repo.observeTurnoActivo(2).first()?.id)

        fakeTime = 5_600_000L
        val cerrado = repo.finalizarTurno(t2.id, totalCapturados = 12)
        assertNotNull(cerrado)
        assertFalse(cerrado!!.estaActivo)
        assertEquals(12, cerrado.totalCapturados)
        assertEquals(5_600_000L, cerrado.finEpoch)
        assertNull(repo.observeTurnoActivo(2).first())
    }
}
