package com.therionlabs.captura_digital_v2.data.repository

import com.therionlabs.captura_digital_v2.CapturaDigitalApp
import com.therionlabs.captura_digital_v2.data.local.DemoSeedGenerator
import com.therionlabs.captura_digital_v2.data.local.InMemorySessionStorage
import com.therionlabs.captura_digital_v2.data.local.SessionStorage
import com.therionlabs.captura_digital_v2.data.local.dao.EventoDao
import com.therionlabs.captura_digital_v2.data.local.dao.TurnoEventoDao
import com.therionlabs.captura_digital_v2.data.model.AppMode
import com.therionlabs.captura_digital_v2.data.model.Evento
import com.therionlabs.captura_digital_v2.data.model.TurnoEvento
import com.therionlabs.captura_digital_v2.data.model.dto.toDomain
import com.therionlabs.captura_digital_v2.data.remote.ProspectoApi
import com.therionlabs.captura_digital_v2.data.remote.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Contrato de repositorio para gestión de eventos/ferias, selección persistida del evento activo
 * y registro de turnos/jornadas de captura en terreno.
 */
interface IEventoRepository {
    fun observeEventos(): Flow<List<Evento>>
    fun observeEventosDisponibles(): Flow<List<Evento>>
    fun observeEventoActivo(): Flow<Evento?>
    suspend fun getEventoActivo(): Evento?
    suspend fun getEventoActivoId(): String
    suspend fun seleccionarEventoActivo(eventoId: String): Evento?
    suspend fun refrescarEventosDesdeServidor(): Boolean
    suspend fun guardarEvento(evento: Evento): Boolean
    suspend fun cambiarEstadoActivo(eventoId: String, activo: Boolean): Boolean
    suspend fun archivarEvento(eventoId: String, archivado: Boolean): Boolean
    suspend fun poblarEventosDemo(resetear: Boolean = false): Int

    fun observeTurnoActivo(captadorId: Int): Flow<TurnoEvento?>
    suspend fun iniciarTurno(captadorId: Int, eventoId: String, metaTurno: Int): TurnoEvento
    suspend fun finalizarTurno(turnoId: String, totalCapturados: Int): TurnoEvento?
}

/**
 * Implementación Offline-First de [IEventoRepository].
 * - En [AppMode.LOCAL_DEMO]: opera 100% sobre Room / memoria local + [DemoSeedGenerator].
 * - En [AppMode.REMOTE_GATEWAY]: sincroniza eventos desde `ProspectoApi.listarEventos` manteniendo Room como caché.
 * - Persiste el `activeEventId` seleccionado en [SessionStorage] (DataStore).
 */
class EventoRepository(
    private val eventoDao: EventoDao? = resolveEventoDao(),
    private val turnoDao: TurnoEventoDao? = resolveTurnoDao(),
    private val api: ProspectoApi? = if (eventoDao != null) RetrofitClient.prospectoApi else null,
    private val sessionStorage: SessionStorage = resolveSessionStorage(),
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val appModeProvider: () -> AppMode = { sessionStorage.currentAppMode },
    private val nowProvider: () -> Long = { System.currentTimeMillis() }
) : IEventoRepository {

    companion object {
        private fun resolveEventoDao(): EventoDao? = try {
            if (CapturaDigitalApp.isDatabaseInitialized()) {
                CapturaDigitalApp.database.eventoDao()
            } else null
        } catch (_: Exception) {
            null
        }

        private fun resolveTurnoDao(): TurnoEventoDao? = try {
            if (CapturaDigitalApp.isDatabaseInitialized()) {
                CapturaDigitalApp.database.turnoEventoDao()
            } else null
        } catch (_: Exception) {
            null
        }

        private fun resolveSessionStorage(): SessionStorage = try {
            CapturaDigitalApp.containerOrNull()?.sessionStorage ?: InMemorySessionStorage()
        } catch (_: Exception) {
            InMemorySessionStorage()
        }

        private fun sortEventos(list: List<Evento>): List<Evento> =
            list.sortedWith(
                compareBy<Evento> { it.archivado }
                    .thenByDescending { it.activo }
                    .thenBy { it.fechaInicio }
            )
    }

    private val _eventosFlow = MutableStateFlow(
        if (eventoDao == null) sortEventos(DemoSeedGenerator.generarEventosDemo()) else emptyList()
    )

    private val _turnosMemoryFlow = MutableStateFlow<List<TurnoEvento>>(emptyList())

    init {
        if (eventoDao != null) {
            coroutineScope.launch {
                eventoDao.observeAll().collect { list ->
                    if (list.isEmpty()) {
                        val seed = DemoSeedGenerator.generarEventosDemo(nowProvider())
                        eventoDao.insertAll(seed)
                    } else {
                        _eventosFlow.value = sortEventos(list)
                    }
                }
            }
        }
    }

    override fun observeEventos(): Flow<List<Evento>> = _eventosFlow

    override fun observeEventosDisponibles(): Flow<List<Evento>> =
        _eventosFlow.map { list -> list.filter { it.estaDisponibleParaCaptura } }

    override fun observeEventoActivo(): Flow<Evento?> =
        combine(_eventosFlow, sessionStorage.sessionState) { eventos, session ->
            resolverEventoActivo(eventos, session.activeEventId)?.also { activo ->
                ProspectoRepository.activeEventId = activo.id
                ProspectoRepository.activeEventName = activo.nombre
            }
        }

    override suspend fun getEventoActivo(): Evento? {
        val eventos = if (_eventosFlow.value.isNotEmpty()) {
            _eventosFlow.value
        } else {
            val fromDb = eventoDao?.getAllList().orEmpty()
            if (fromDb.isNotEmpty()) {
                _eventosFlow.value = sortEventos(fromDb)
                _eventosFlow.value
            } else {
                val seed = sortEventos(DemoSeedGenerator.generarEventosDemo(nowProvider()))
                eventoDao?.insertAll(seed)
                _eventosFlow.value = seed
                seed
            }
        }
        return resolverEventoActivo(eventos, sessionStorage.currentActiveEventId)
    }

    override suspend fun getEventoActivoId(): String =
        getEventoActivo()?.id ?: sessionStorage.currentActiveEventId ?: DemoSeedGenerator.DEFAULT_EVENT_ID

    override suspend fun seleccionarEventoActivo(eventoId: String): Evento? {
        val eventos = if (_eventosFlow.value.isNotEmpty()) {
            _eventosFlow.value
        } else {
            eventoDao?.getAllList().orEmpty()
        }
        val target = eventos.firstOrNull { it.id == eventoId && !it.archivado }
            ?: eventoDao?.getById(eventoId)?.takeIf { !it.archivado }
            ?: return null

        sessionStorage.setActiveEventId(target.id)
        ProspectoRepository.activeEventId = target.id
        ProspectoRepository.activeEventName = target.nombre
        return target
    }

    override suspend fun refrescarEventosDesdeServidor(): Boolean {
        if (appModeProvider() == AppMode.LOCAL_DEMO) {
            if (eventoDao != null) {
                val current = eventoDao.getAllList()
                if (current.isEmpty()) {
                    val seed = DemoSeedGenerator.generarEventosDemo(nowProvider())
                    eventoDao.insertAll(seed)
                    _eventosFlow.value = sortEventos(seed)
                } else {
                    _eventosFlow.value = sortEventos(current)
                }
            }
            return true
        }

        val remoteApi = api ?: return false
        return try {
            val remotos = remoteApi.listarEventos(activo = null)
                .ifEmpty { remoteApi.listarEventos(activo = true) }
                .map { it.toDomain(nowProvider()) }

            if (remotos.isNotEmpty()) {
                eventoDao?.insertAll(remotos)
                val merged = mergeEventos(_eventosFlow.value, remotos)
                _eventosFlow.value = sortEventos(merged)

                val selectedId = sessionStorage.currentActiveEventId
                if (selectedId == null || merged.none { it.id == selectedId && it.estaDisponibleParaCaptura }) {
                    val firstActive = merged.firstOrNull { it.estaDisponibleParaCaptura }
                    if (firstActive != null) {
                        sessionStorage.setActiveEventId(firstActive.id)
                    }
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun guardarEvento(evento: Evento): Boolean {
        val normalized = if (evento.archivado) evento.copy(activo = false) else evento
        eventoDao?.insertOrUpdate(normalized)
        upsertEventoInMemory(normalized)
        revalidarEventoActivoTrasCambio(normalized)
        return true
    }

    override suspend fun cambiarEstadoActivo(eventoId: String, activo: Boolean): Boolean {
        val current = _eventosFlow.value.firstOrNull { it.id == eventoId }
            ?: eventoDao?.getById(eventoId)
            ?: return false

        if (current.archivado && activo) return false

        val updated = current.copy(activo = activo)
        eventoDao?.updateActivo(eventoId, activo)
        upsertEventoInMemory(updated)
        revalidarEventoActivoTrasCambio(updated)
        return true
    }

    override suspend fun archivarEvento(eventoId: String, archivado: Boolean): Boolean {
        val current = _eventosFlow.value.firstOrNull { it.id == eventoId }
            ?: eventoDao?.getById(eventoId)
            ?: return false

        val updated = current.copy(
            archivado = archivado,
            activo = if (archivado) false else current.activo
        )
        eventoDao?.archivar(eventoId, archivado)
        upsertEventoInMemory(updated)
        revalidarEventoActivoTrasCambio(updated)
        return true
    }

    override suspend fun poblarEventosDemo(resetear: Boolean): Int {
        val seed = sortEventos(DemoSeedGenerator.generarEventosDemo(nowProvider()))
        if (eventoDao != null) {
            if (resetear) {
                eventoDao.deleteAll()
            }
            eventoDao.insertAll(seed)
            _eventosFlow.value = sortEventos(eventoDao.getAllList())
        } else {
            _eventosFlow.value = if (resetear) seed else mergeEventos(_eventosFlow.value, seed)
        }
        return seed.size
    }

    override fun observeTurnoActivo(captadorId: Int): Flow<TurnoEvento?> {
        return if (turnoDao != null) {
            turnoDao.observeTurnoActivo(captadorId)
        } else {
            _turnosMemoryFlow.map { list ->
                list.lastOrNull { it.captadorId == captadorId && it.finEpoch == null }
            }
        }
    }

    override suspend fun iniciarTurno(captadorId: Int, eventoId: String, metaTurno: Int): TurnoEvento {
        val now = nowProvider()
        // Garantiza un único turno activo por captador cerrando alguno huérfano si existiera
        val previo = if (turnoDao != null) {
            turnoDao.getTurnoActivo(captadorId)
        } else {
            _turnosMemoryFlow.value.lastOrNull { it.captadorId == captadorId && it.finEpoch == null }
        }
        if (previo != null) {
            val cerrado = previo.copy(finEpoch = now)
            turnoDao?.insertOrUpdate(cerrado)
            upsertTurnoInMemory(cerrado)
        }

        val nuevo = TurnoEvento(
            eventoId = eventoId,
            captadorId = captadorId,
            inicioEpoch = now,
            finEpoch = null,
            metaTurno = metaTurno.coerceAtLeast(1),
            totalCapturados = 0
        )
        turnoDao?.insertOrUpdate(nuevo)
        upsertTurnoInMemory(nuevo)
        return nuevo
    }

    override suspend fun finalizarTurno(turnoId: String, totalCapturados: Int): TurnoEvento? {
        val actual = if (turnoDao != null) {
            turnoDao.getById(turnoId)
        } else {
            _turnosMemoryFlow.value.firstOrNull { it.id == turnoId }
        } ?: return null

        val cerrado = actual.copy(
            finEpoch = nowProvider(),
            totalCapturados = totalCapturados.coerceAtLeast(0)
        )
        turnoDao?.insertOrUpdate(cerrado)
        upsertTurnoInMemory(cerrado)
        return cerrado
    }

    private fun resolverEventoActivo(eventos: List<Evento>, selectedId: String?): Evento? {
        val disponibles = eventos.filter { it.estaDisponibleParaCaptura }
        if (selectedId != null) {
            disponibles.firstOrNull { it.id == selectedId }?.let { return it }
            eventos.firstOrNull { it.id == selectedId && !it.archivado }?.let { return it }
        }
        return disponibles.firstOrNull { it.id == DemoSeedGenerator.DEFAULT_EVENT_ID }
            ?: disponibles.firstOrNull()
            ?: eventos.firstOrNull { !it.archivado }
            ?: eventos.firstOrNull()
    }

    private suspend fun revalidarEventoActivoTrasCambio(eventoActualizado: Evento) {
        if (sessionStorage.currentActiveEventId == eventoActualizado.id &&
            !eventoActualizado.estaDisponibleParaCaptura
        ) {
            val siguiente = _eventosFlow.value.firstOrNull { it.estaDisponibleParaCaptura }
            sessionStorage.setActiveEventId(siguiente?.id)
        }
    }

    private fun upsertEventoInMemory(evento: Evento) {
        val mutable = _eventosFlow.value.toMutableList()
        val idx = mutable.indexOfFirst { it.id == evento.id }
        if (idx != -1) {
            mutable[idx] = evento
        } else {
            mutable.add(evento)
        }
        _eventosFlow.value = sortEventos(mutable)
    }

    private fun upsertTurnoInMemory(turno: TurnoEvento) {
        val mutable = _turnosMemoryFlow.value.toMutableList()
        val idx = mutable.indexOfFirst { it.id == turno.id }
        if (idx != -1) {
            mutable[idx] = turno
        } else {
            mutable.add(turno)
        }
        _turnosMemoryFlow.value = mutable
    }

    private fun mergeEventos(locales: List<Evento>, remotos: List<Evento>): List<Evento> {
        val porId = locales.associateBy { it.id }.toMutableMap()
        remotos.forEach { porId[it.id] = it }
        return sortEventos(porId.values.toList())
    }
}
