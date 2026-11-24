package com.therionlabs.captura_digital_v2.data.repository

import com.therionlabs.captura_digital_v2.CapturaDigitalApp
import com.therionlabs.captura_digital_v2.data.local.DemoSeedGenerator
import com.therionlabs.captura_digital_v2.data.local.dao.ProspectoDao
import com.therionlabs.captura_digital_v2.data.model.AppMode
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SyncStatus
import com.therionlabs.captura_digital_v2.data.model.dto.BatchSyncRequestDto
import com.therionlabs.captura_digital_v2.data.model.dto.EventoResponseDto
import com.therionlabs.captura_digital_v2.data.model.dto.toDomain
import com.therionlabs.captura_digital_v2.data.model.dto.toDto
import com.therionlabs.captura_digital_v2.data.remote.ProspectoApi
import com.therionlabs.captura_digital_v2.data.remote.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.HttpException
import java.io.IOException

data class SyncResult(
    val totalEnviados: Int,
    val exitosos: Int,
    val fallidos: Int,
    val mensaje: String,
    val huboErrorRed: Boolean = false
)

interface IProspectoRepository {
    fun guardarProspecto(prospecto: Prospecto): Boolean
    fun getAll(): List<Prospecto>
    fun getByCaptador(captadorId: Int): List<Prospecto>
    fun getPendingSyncCount(): Int

    fun observeProspectos(): Flow<List<Prospecto>> = flowOf(getAll())

    suspend fun guardarProspectoSuspend(prospecto: Prospecto): Boolean = guardarProspecto(prospecto)

    suspend fun sincronizarPendientes(captadorId: Int, eventoId: String? = null): SyncResult =
        SyncResult(0, 0, 0, "Sin operación remota")

    /**
     * Sincroniza únicamente registros nuevos pendientes (PENDING_SYNC) sin reintentar SYNC_ERROR en bucle.
     */
    suspend fun sincronizarNuevosPendientes(captadorId: Int, eventoId: String? = null): SyncResult =
        SyncResult(0, 0, 0, "Sin operación remota")

    suspend fun refrescarDesdeServidor(captadorId: Int? = null): Boolean = false

    suspend fun obtenerEventoActivo(): EventoResponseDto? = null

    suspend fun actualizarProspecto(prospecto: Prospecto): Boolean = guardarProspecto(prospecto)

    suspend fun eliminarProspecto(clientUuid: String): Boolean = false

    suspend fun poblarDatosDemo(resetear: Boolean = true): Int = 0
}

/**
 * Repositorio de Prospectos Offline-First con soporte dual:
 * - [AppMode.LOCAL_DEMO]: persistencia Room/memoria, métricas y simulación de sync 100% locales.
 * - [AppMode.REMOTE_GATEWAY]: Room + sincronización por lote y CRUD remoto vía Retrofit + API Gateway.
 */
class ProspectoRepository(
    private val dao: ProspectoDao? = resolveDao(),
    private val api: ProspectoApi? = resolveApi(dao),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val appModeProvider: () -> AppMode = { AppMode.REMOTE_GATEWAY },
    private val activeEventIdProvider: () -> String = { activeEventId }
) : IProspectoRepository {

    companion object {
        @Volatile
        var activeEventId: String = DemoSeedGenerator.DEFAULT_EVENT_ID

        @Volatile
        var activeEventName: String = DemoSeedGenerator.DEFAULT_EVENT_NAME

        private fun resolveDao(): ProspectoDao? {
            return try {
                if (CapturaDigitalApp.isDatabaseInitialized()) {
                    CapturaDigitalApp.database.prospectoDao()
                } else null
            } catch (_: Exception) {
                null
            }
        }

        private fun resolveApi(dao: ProspectoDao?): ProspectoApi? {
            return if (dao != null) RetrofitClient.prospectoApi else null
        }

        private val fallbackMemoryList = DemoSeedGenerator.generarProspectosDemo().toMutableList()
    }

    private val _prospectosFlow = MutableStateFlow<List<Prospecto>>(
        if (dao == null) fallbackMemoryList.toList() else emptyList()
    )
    val prospectosFlow: StateFlow<List<Prospecto>> = _prospectosFlow.asStateFlow()

    override fun observeProspectos(): Flow<List<Prospecto>> = prospectosFlow

    init {
        if (dao != null) {
            coroutineScope.launch {
                dao.getAllFlow().collect { prospectos ->
                    _prospectosFlow.value = prospectos
                }
            }
        }
    }

    override fun guardarProspecto(prospecto: Prospecto): Boolean {
        upsertInMemory(prospecto)

        if (dao != null) {
            coroutineScope.launch {
                dao.insertOrUpdate(prospecto)
            }
        }
        return true
    }

    private fun upsertInMemory(prospecto: Prospecto) {
        val current = _prospectosFlow.value.toMutableList()
        val index = current.indexOfFirst { it.rut == prospecto.rut }
        if (index != -1) {
            current[index] = prospecto
        } else {
            current.add(0, prospecto)
        }
        _prospectosFlow.value = current

        if (dao == null) {
            val fallbackIndex = fallbackMemoryList.indexOfFirst { it.rut == prospecto.rut }
            if (fallbackIndex != -1) {
                fallbackMemoryList[fallbackIndex] = prospecto
            } else {
                fallbackMemoryList.add(0, prospecto)
            }
        }
    }

    override fun getAll(): List<Prospecto> {
        return if (dao != null) {
            val cached = _prospectosFlow.value
            if (cached.isNotEmpty()) {
                cached
            } else {
                try {
                    val list = runBlocking(Dispatchers.IO) { dao.getAllList() }
                    _prospectosFlow.value = list
                    list
                } catch (_: Exception) {
                    cached
                }
            }
        } else {
            _prospectosFlow.value
        }
    }

    override fun getByCaptador(captadorId: Int): List<Prospecto> =
        getAll().filter { it.captadorId == captadorId }

    override fun getPendingSyncCount(): Int =
        getAll().count { it.syncStatus != SyncStatus.SYNCED }

    override suspend fun guardarProspectoSuspend(prospecto: Prospecto): Boolean {
        upsertInMemory(prospecto)
        dao?.insertOrUpdate(prospecto)
        return true
    }

    override suspend fun sincronizarPendientes(captadorId: Int, eventoId: String?): SyncResult =
        ejecutarSyncLote(captadorId = captadorId, eventoId = eventoId, incluirErroresPrevios = true)

    override suspend fun sincronizarNuevosPendientes(captadorId: Int, eventoId: String?): SyncResult =
        ejecutarSyncLote(captadorId = captadorId, eventoId = eventoId, incluirErroresPrevios = false)

    private suspend fun ejecutarSyncLote(
        captadorId: Int,
        eventoId: String?,
        incluirErroresPrevios: Boolean
    ): SyncResult {
        val allPending = dao?.getPendingSyncList()
            ?: _prospectosFlow.value.filter { it.syncStatus != SyncStatus.SYNCED }
        val pending = if (incluirErroresPrevios) {
            allPending
        } else {
            allPending.filter { it.syncStatus == SyncStatus.PENDING_SYNC }
        }

        if (appModeProvider() == AppMode.LOCAL_DEMO) {
            if (pending.isEmpty()) {
                return SyncResult(0, 0, 0, "Todo al día. No hay registros pendientes.")
            }
            val ids = pending.map { it.id }
            dao?.updateSyncStatusByIds(ids, SyncStatus.SYNCED)
            val idsSet = ids.toSet()
            _prospectosFlow.value = _prospectosFlow.value.map { item ->
                if (item.id in idsSet) item.copy(syncStatus = SyncStatus.SYNCED) else item
            }
            if (dao == null) {
                syncFallbackListStatuses(exitosos = idsSet, fallidos = emptySet())
            }
            return SyncResult(
                totalEnviados = pending.size,
                exitosos = pending.size,
                fallidos = 0,
                mensaje = "Sincronización demo local completada (${pending.size} registro(s)).",
                huboErrorRed = false
            )
        }

        val remoteApi = api ?: return SyncResult(0, 0, 0, "Cliente remoto no disponible")
        if (pending.isEmpty()) {
            return SyncResult(0, 0, 0, "Todo al día. No hay registros pendientes.")
        }

        return try {
            val request = BatchSyncRequestDto(
                captadorId = captadorId,
                eventoId = eventoId,
                prospectos = pending.map { it.toDto() }
            )
            val response = remoteApi.sincronizarLote(request)

            if (response.exitososUuids.isNotEmpty()) {
                dao?.updateSyncStatusByIds(response.exitososUuids, SyncStatus.SYNCED)
            }
            if (response.fallidosUuids.isNotEmpty()) {
                dao?.updateSyncStatusByIds(response.fallidosUuids, SyncStatus.SYNC_ERROR)
            }

            val exitososSet = response.exitososUuids.toSet()
            val fallidosSet = response.fallidosUuids.toSet()
            _prospectosFlow.value = _prospectosFlow.value.map { item ->
                when (item.id) {
                    in exitososSet -> item.copy(syncStatus = SyncStatus.SYNCED)
                    in fallidosSet -> item.copy(syncStatus = SyncStatus.SYNC_ERROR)
                    else -> item
                }
            }
            if (dao == null) {
                syncFallbackListStatuses(exitososSet, fallidosSet)
            }

            val errores = response.erroresPorUuid ?: emptyMap()
            if (response.fallidosUuids.isNotEmpty()) {
                val pendingById = pending.associateBy { it.id }
                val detalles = response.fallidosUuids.mapNotNull { uuid ->
                    val motivo = errores[uuid] ?: return@mapNotNull null
                    val etiqueta = pendingById[uuid]?.let { "${it.nombres} (${it.rut})" } ?: uuid
                    "$etiqueta: $motivo"
                }
                detalles.forEach { safeLogWarn("ProspectoSync", "Fallo parcial -> $it") }
                val mensajeFinal = if (detalles.isNotEmpty()) {
                    "${response.mensaje} (${detalles.joinToString("; ")})"
                } else {
                    response.mensaje
                }
                return SyncResult(
                    totalEnviados = response.totalProcesados,
                    exitosos = response.exitososUuids.size,
                    fallidos = response.fallidosUuids.size,
                    mensaje = mensajeFinal
                )
            }

            SyncResult(
                totalEnviados = response.totalProcesados,
                exitosos = response.exitososUuids.size,
                fallidos = response.fallidosUuids.size,
                mensaje = response.mensaje
            )
        } catch (ex: HttpException) {
            safeLogError("ProspectoSync", "Error HTTP ${ex.code()} al sincronizar", ex)
            val msg = if (ex.code() == 401 || ex.code() == 403) {
                "Sesión expirada o sin permisos (HTTP ${ex.code()}). Vuelve a iniciar sesión."
            } else {
                "Error del servidor (HTTP ${ex.code()}) al sincronizar."
            }
            SyncResult(
                totalEnviados = pending.size,
                exitosos = 0,
                fallidos = pending.size,
                mensaje = msg
            )
        } catch (ex: IOException) {
            safeLogWarn("ProspectoSync", "Error de red al sincronizar: ${ex.message}")
            SyncResult(
                totalEnviados = pending.size,
                exitosos = 0,
                fallidos = pending.size,
                mensaje = "Sin conexión con el servidor; tus datos siguen seguros localmente.",
                huboErrorRed = true
            )
        } catch (ex: Exception) {
            safeLogError("ProspectoSync", "Error inesperado al sincronizar", ex)
            SyncResult(
                totalEnviados = pending.size,
                exitosos = 0,
                fallidos = pending.size,
                mensaje = "Error al sincronizar: ${ex.message ?: "fallo inesperado"}"
            )
        }
    }

    override suspend fun refrescarDesdeServidor(captadorId: Int?): Boolean {
        if (appModeProvider() == AppMode.LOCAL_DEMO) {
            if (dao != null) {
                _prospectosFlow.value = dao.getAllList()
            }
            return true
        }

        val remoteApi = api ?: return false
        return try {
            val remotos = remoteApi.listar(captadorId = captadorId)
                .map { it.toDomain(SyncStatus.SYNCED) }
            val remoteIds = remotos.map { it.id }.toSet()

            val actuales = dao?.getAllList() ?: _prospectosFlow.value
            val pendientesPorUuid = actuales
                .filter { it.syncStatus != SyncStatus.SYNCED }
                .associateBy { it.id }

            if (dao != null) {
                val obsoletosSyncedIds = actuales
                    .filter {
                        it.syncStatus == SyncStatus.SYNCED &&
                            (captadorId == null || it.captadorId == captadorId) &&
                            it.id !in remoteIds
                    }
                    .map { it.id }
                obsoletosSyncedIds.forEach { dao.deleteById(it) }

                val nuevosOActualizados = remotos.filter { it.id !in pendientesPorUuid }
                if (nuevosOActualizados.isNotEmpty()) {
                    dao.insertAll(nuevosOActualizados)
                }
            }

            // Conservamos cambios locales pendientes sin pisarlos con la versión remota
            val combinados = (remotos.filter { it.id !in pendientesPorUuid } + pendientesPorUuid.values)
                .sortedByDescending { it.fechaCaptura }
            _prospectosFlow.value = combinados
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun obtenerEventoActivo(): EventoResponseDto? {
        if (appModeProvider() == AppMode.LOCAL_DEMO) {
            val resolvedId = activeEventIdProvider()
            val resolvedName = if (resolvedId == activeEventId) activeEventName else resolvedId
            return EventoResponseDto(
                id = resolvedId,
                nombre = resolvedName,
                establecimiento = "Duoc UC Sede Viña del Mar",
                comuna = "Viña del Mar",
                sedeResponsable = "Viña del Mar",
                fechaEvento = null,
                activo = true
            )
        }

        val remoteApi = api ?: return null
        return try {
            val activo = remoteApi.listarEventos(activo = true).firstOrNull()
            if (activo != null) {
                activeEventId = activo.id
                activeEventName = activo.nombre
            }
            activo
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun actualizarProspecto(prospecto: Prospecto): Boolean {
        val updated = if (appModeProvider() == AppMode.LOCAL_DEMO) {
            prospecto.copy(syncStatus = SyncStatus.PENDING_SYNC)
        } else {
            val remoteApi = api
            if (remoteApi != null) {
                try {
                    remoteApi.actualizar(prospecto.id, prospecto.toDto()).toDomain(SyncStatus.SYNCED)
                } catch (_: Exception) {
                    prospecto.copy(syncStatus = SyncStatus.PENDING_SYNC)
                }
            } else {
                prospecto.copy(syncStatus = SyncStatus.PENDING_SYNC)
            }
        }
        return guardarProspectoSuspend(updated)
    }

    override suspend fun eliminarProspecto(clientUuid: String): Boolean {
        if (appModeProvider() == AppMode.REMOTE_GATEWAY) {
            api?.let { remoteApi ->
                runCatching { remoteApi.eliminar(clientUuid) }
            }
        }
        dao?.deleteById(clientUuid)
        _prospectosFlow.value = _prospectosFlow.value.filterNot { it.id == clientUuid }
        fallbackMemoryList.removeAll { it.id == clientUuid }
        return true
    }

    override suspend fun poblarDatosDemo(resetear: Boolean): Int {
        val seed = DemoSeedGenerator.generarProspectosDemo(eventoId = activeEventIdProvider())
        if (dao != null) {
            if (resetear) {
                dao.deleteAll()
            }
            dao.insertAll(seed)
            _prospectosFlow.value = dao.getAllList()
        } else {
            if (resetear) {
                fallbackMemoryList.clear()
                fallbackMemoryList.addAll(seed)
            } else {
                seed.forEach { item ->
                    if (fallbackMemoryList.none { it.id == item.id || it.rut == item.rut }) {
                        fallbackMemoryList.add(item)
                    }
                }
            }
            _prospectosFlow.value = fallbackMemoryList.toList()
        }
        return seed.size
    }

    private fun syncFallbackListStatuses(exitosos: Set<String>, fallidos: Set<String>) {
        for (i in fallbackMemoryList.indices) {
            val item = fallbackMemoryList[i]
            when (item.id) {
                in exitosos -> fallbackMemoryList[i] = item.copy(syncStatus = SyncStatus.SYNCED)
                in fallidos -> fallbackMemoryList[i] = item.copy(syncStatus = SyncStatus.SYNC_ERROR)
            }
        }
    }

    private fun safeLogWarn(tag: String, msg: String) {
        runCatching { android.util.Log.w(tag, msg) }
    }

    private fun safeLogError(tag: String, msg: String, throwable: Throwable) {
        runCatching { android.util.Log.e(tag, msg, throwable) }
    }
}
