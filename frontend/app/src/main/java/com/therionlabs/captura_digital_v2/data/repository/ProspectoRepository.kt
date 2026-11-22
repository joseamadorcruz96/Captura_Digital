package com.therionlabs.captura_digital_v2.data.repository

import com.therionlabs.captura_digital_v2.CapturaDigitalApp
import com.therionlabs.captura_digital_v2.data.local.dao.ProspectoDao
import com.therionlabs.captura_digital_v2.data.model.Genero
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.data.model.SyncStatus
import com.therionlabs.captura_digital_v2.data.model.dto.BatchSyncRequestDto
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

data class SyncResult(
    val totalEnviados: Int,
    val exitosos: Int,
    val fallidos: Int,
    val mensaje: String
)

interface IProspectoRepository {
    fun guardarProspecto(prospecto: Prospecto): Boolean
    fun getAll(): List<Prospecto>
    fun getByCaptador(captadorId: Int): List<Prospecto>
    fun getPendingSyncCount(): Int

    fun observeProspectos(): Flow<List<Prospecto>> = flowOf(getAll())

    suspend fun sincronizarPendientes(captadorId: Int, eventoId: String? = null): SyncResult =
        SyncResult(0, 0, 0, "Sin operación remota")

    suspend fun refrescarDesdeServidor(captadorId: Int? = null): Boolean = false

    suspend fun actualizarProspecto(prospecto: Prospecto): Boolean = guardarProspecto(prospecto)

    suspend fun eliminarProspecto(clientUuid: String): Boolean = false
}

/**
 * Repositorio de Prospectos Offline-First:
 * - Persistencia inmediata en Room SQLite.
 * - Sincronización por lote (Batch Sync) y CRUD en tiempo real vía Retrofit + API Gateway.
 */
class ProspectoRepository(
    private val dao: ProspectoDao? = resolveDao(),
    private val api: ProspectoApi? = resolveApi(dao),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : IProspectoRepository {

    companion object {
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
            // En tests unitarios sin Room ni mock explícito evitamos inicializar OkHttp innecesariamente si no se usa
            return if (dao != null) RetrofitClient.prospectoApi else null
        }

        private val fallbackMemoryList = mutableListOf(
            Prospecto(
                id = "demo-uuid-1",
                captadorId = 2,
                eventoId = "EVT-DUOC-2026",
                rut = "20.123.456-5",
                nombres = "Camila Andrea",
                apellidos = "Valenzuela Soto",
                genero = Genero.FEMENINO,
                fechaNacimiento = "15/04/2008",
                comunaResidencia = "Viña del Mar",
                celular = "+56987654321",
                email = "camila.valenzuela@gmail.com",
                consentimientoDatos = true,
                situacionAcademica = SituacionAcademica.CUARTO_MEDIO,
                establecimientoEducacional = "Colegio Rubén Castro",
                comunaEstablecimiento = "Viña del Mar",
                jornadaPreferencia = Jornada.DIURNA,
                sedeInteres = "Viña del Mar",
                nivelInteres = NivelInteres.ALTO,
                perfilMotivacional = PerfilMotivacional.VOCACIONAL,
                carreraPrioridad1 = "Ingeniería en Informática",
                carreraPrioridad2 = "Analista Programador",
                syncStatus = SyncStatus.PENDING_SYNC
            ),
            Prospecto(
                id = "demo-uuid-2",
                captadorId = 2,
                eventoId = "EVT-DUOC-2026",
                rut = "11.111.111-1",
                nombres = "Lucas Ignacio",
                apellidos = "González Silva",
                genero = Genero.MASCULINO,
                fechaNacimiento = "22/09/2007",
                comunaResidencia = "Valparaíso",
                celular = "+56912345678",
                email = "lucas.gonzalez@gmail.com",
                consentimientoDatos = true,
                situacionAcademica = SituacionAcademica.EGRESADO_EM,
                establecimientoEducacional = "Liceo Eduardo de la Barra",
                comunaEstablecimiento = "Valparaíso",
                jornadaPreferencia = Jornada.DIURNA,
                sedeInteres = "Valparaíso",
                nivelInteres = NivelInteres.MEDIO,
                perfilMotivacional = PerfilMotivacional.INVERSION,
                carreraPrioridad1 = "Gastronomía Internacional",
                carreraPrioridad2 = null,
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )
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
        val current = _prospectosFlow.value.toMutableList()
        val index = current.indexOfFirst { it.rut == prospecto.rut }
        if (index != -1) {
            current[index] = prospecto
        } else {
            current.add(0, prospecto)
        }
        _prospectosFlow.value = current

        if (dao != null) {
            coroutineScope.launch {
                dao.insertOrUpdate(prospecto)
            }
        } else {
            val fallbackIndex = fallbackMemoryList.indexOfFirst { it.rut == prospecto.rut }
            if (fallbackIndex != -1) {
                fallbackMemoryList[fallbackIndex] = prospecto
            } else {
                fallbackMemoryList.add(0, prospecto)
            }
        }
        return true
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

    suspend fun guardarProspectoSuspend(prospecto: Prospecto): Boolean {
        guardarProspecto(prospecto)
        dao?.insertOrUpdate(prospecto)
        return true
    }

    override suspend fun sincronizarPendientes(captadorId: Int, eventoId: String?): SyncResult {
        val remoteApi = api ?: return SyncResult(0, 0, 0, "Cliente remoto no disponible")
        val pending = dao?.getPendingSyncList()
            ?: _prospectosFlow.value.filter { it.syncStatus != SyncStatus.SYNCED }

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

            SyncResult(
                totalEnviados = response.totalProcesados,
                exitosos = response.exitososUuids.size,
                fallidos = response.fallidosUuids.size,
                mensaje = response.mensaje
            )
        } catch (ex: Exception) {
            SyncResult(
                totalEnviados = pending.size,
                exitosos = 0,
                fallidos = pending.size,
                mensaje = "Sin conexión con el servidor; tus datos siguen seguros localmente."
            )
        }
    }

    override suspend fun refrescarDesdeServidor(captadorId: Int?): Boolean {
        val remoteApi = api ?: return false
        return try {
            val remotos = remoteApi.listar(captadorId = captadorId)
                .map { it.toDomain(SyncStatus.SYNCED) }

            val actuales = dao?.getAllList() ?: _prospectosFlow.value
            val pendientesPorUuid = actuales
                .filter { it.syncStatus != SyncStatus.SYNCED }
                .associateBy { it.id }

            // Conservamos cambios locales pendientes sin pisarlos con la versión remota
            val combinados = (remotos.filter { it.id !in pendientesPorUuid } + pendientesPorUuid.values)
                .sortedByDescending { it.fechaCaptura }

            if (dao != null && remotos.isNotEmpty()) {
                dao.insertAll(remotos.filter { it.id !in pendientesPorUuid })
            }
            _prospectosFlow.value = combinados
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun actualizarProspecto(prospecto: Prospecto): Boolean {
        val remoteApi = api
        val updated = if (remoteApi != null) {
            try {
                remoteApi.actualizar(prospecto.id, prospecto.toDto()).toDomain(SyncStatus.SYNCED)
            } catch (_: Exception) {
                prospecto.copy(syncStatus = SyncStatus.PENDING_SYNC)
            }
        } else {
            prospecto.copy(syncStatus = SyncStatus.PENDING_SYNC)
        }
        return guardarProspectoSuspend(updated)
    }

    override suspend fun eliminarProspecto(clientUuid: String): Boolean {
        api?.let { remoteApi ->
            runCatching { remoteApi.eliminar(clientUuid) }
        }
        dao?.deleteById(clientUuid)
        _prospectosFlow.value = _prospectosFlow.value.filterNot { it.id == clientUuid }
        fallbackMemoryList.removeAll { it.id == clientUuid }
        return true
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
}
