package com.therionlabs.captura_digital_v2.data.repository

import com.therionlabs.captura_digital_v2.data.model.Genero
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.data.model.SyncStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface IProspectoRepository {
    fun guardarProspecto(prospecto: Prospecto): Boolean
    fun getAll(): List<Prospecto>
    fun getByCaptador(captadorId: Int): List<Prospecto>
    fun getPendingSyncCount(): Int
}

class ProspectoRepository : IProspectoRepository {

    companion object {
        // Datos en memoria compartidos durante la ejecución de la app (previo a Room)
        private val prospectosList = mutableListOf(
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

    private val _prospectosFlow = MutableStateFlow<List<Prospecto>>(prospectosList)
    val prospectosFlow: StateFlow<List<Prospecto>> = _prospectosFlow.asStateFlow()

    override fun guardarProspecto(prospecto: Prospecto): Boolean {
        // Evitar duplicados por RUT si ya existe (upsert / actualización)
        val index = prospectosList.indexOfFirst { it.rut == prospecto.rut }
        if (index != -1) {
            prospectosList[index] = prospecto
        } else {
            prospectosList.add(0, prospecto)
        }
        _prospectosFlow.value = prospectosList.toList()
        return true
    }

    override fun getAll(): List<Prospecto> = prospectosList.toList()

    override fun getByCaptador(captadorId: Int): List<Prospecto> =
        prospectosList.filter { it.captadorId == captadorId }

    override fun getPendingSyncCount(): Int =
        prospectosList.count { it.syncStatus == SyncStatus.PENDING_SYNC }
}
