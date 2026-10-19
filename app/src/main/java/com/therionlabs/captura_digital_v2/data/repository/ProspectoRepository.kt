package com.therionlabs.captura_digital_v2.data.repository

import com.therionlabs.captura_digital_v2.CapturaDigitalApp
import com.therionlabs.captura_digital_v2.data.local.dao.ProspectoDao
import com.therionlabs.captura_digital_v2.data.local.mapper.toDomainList
import com.therionlabs.captura_digital_v2.data.local.mapper.toEntity
import com.therionlabs.captura_digital_v2.data.model.Genero
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.data.model.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

interface IProspectoRepository {
    fun guardarProspecto(prospecto: Prospecto): Boolean
    fun getAll(): List<Prospecto>
    fun getByCaptador(captadorId: Int): List<Prospecto>
    fun getPendingSyncCount(): Int
}

/**
 * Repositorio de Prospectos conectado a SQLite vía Room.
 * Implementa el patrón Offline-First manteniendo retrocompatibilidad total
 * con las vistas de Inicio, Formulario y Dashboard.
 */
class ProspectoRepository(
    private val dao: ProspectoDao? = resolveDao(),
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

        // Datos en memoria compartidos como respaldo en caso de tests unitarios puros sin Room
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

    private val _prospectosFlow = MutableStateFlow<List<Prospecto>>(fallbackMemoryList)
    val prospectosFlow: StateFlow<List<Prospecto>> = _prospectosFlow.asStateFlow()

    init {
        if (dao != null) {
            coroutineScope.launch {
                dao.getAllFlow().collect { entities ->
                    if (entities.isNotEmpty()) {
                        _prospectosFlow.value = entities.toDomainList()
                    }
                }
            }
        }
    }

    override fun guardarProspecto(prospecto: Prospecto): Boolean {
        // Actualización optimista inmediata en StateFlow para reactividad de UI
        val current = _prospectosFlow.value.toMutableList()
        val index = current.indexOfFirst { it.rut == prospecto.rut }
        if (index != -1) {
            current[index] = prospecto
        } else {
            current.add(0, prospecto)
        }
        _prospectosFlow.value = current

        // Persistencia asíncrona en base de datos Room SQLite
        if (dao != null) {
            coroutineScope.launch {
                dao.insertOrUpdate(prospecto.toEntity())
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
            if (cached.isNotEmpty() && cached !== fallbackMemoryList) {
                cached
            } else {
                try {
                    val list = runBlocking(Dispatchers.IO) { dao.getAllList().toDomainList() }
                    if (list.isNotEmpty()) {
                        _prospectosFlow.value = list
                        list
                    } else cached
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
        getAll().count { it.syncStatus == SyncStatus.PENDING_SYNC }

    suspend fun guardarProspectoSuspend(prospecto: Prospecto): Boolean {
        guardarProspecto(prospecto)
        dao?.insertOrUpdate(prospecto.toEntity())
        return true
    }
}
