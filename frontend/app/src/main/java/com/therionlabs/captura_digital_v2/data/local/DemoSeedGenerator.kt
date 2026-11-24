package com.therionlabs.captura_digital_v2.data.local

import com.therionlabs.captura_digital_v2.data.model.Evento
import com.therionlabs.captura_digital_v2.data.model.Genero
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.data.model.SyncStatus
import com.therionlabs.captura_digital_v2.data.model.TipoEvento
import com.therionlabs.captura_digital_v2.util.RutUtils

/**
 * Generador centralizado de datos demo realistas para App-First / LOCAL_DEMO.
 * Todos los RUTs generados cumplen validación Módulo 11 mediante [RutUtils].
 */
object DemoSeedGenerator {

    const val DEFAULT_EVENT_ID: String = "EVT-DUOC-2026"
    const val DEFAULT_EVENT_NAME: String = "Feria Vocacional Duoc UC 2026"

    /**
     * Genera eventos demo realistas (ferias, colegios, casa abierta, inactivos y archivados)
     * para operar sin backend en LOCAL_DEMO y como seed inicial de Room v4+.
     */
    fun generarEventosDemo(ahoraEpochMs: Long = System.currentTimeMillis()): List<Evento> {
        val unDiaMs = 24 * 60 * 60 * 1000L
        return listOf(
            Evento(
                id = DEFAULT_EVENT_ID,
                nombre = DEFAULT_EVENT_NAME,
                tipo = TipoEvento.FERIA,
                sedeAsociada = "Viña del Mar",
                ubicacion = "Duoc UC Sede Viña del Mar, Álvarez 2366",
                fechaInicio = ahoraEpochMs - unDiaMs,
                fechaFin = ahoraEpochMs + 2 * unDiaMs,
                metaProspectos = 40,
                activo = true,
                archivado = false
            ),
            Evento(
                id = "EVT-COL-RUBEN-2026",
                nombre = "Charla Vocacional Colegio Rubén Castro",
                tipo = TipoEvento.COLEGIO,
                sedeAsociada = "Viña del Mar",
                ubicacion = "Colegio Rubén Castro, Viña del Mar",
                fechaInicio = ahoraEpochMs,
                fechaFin = ahoraEpochMs + unDiaMs,
                metaProspectos = 25,
                activo = true,
                archivado = false
            ),
            Evento(
                id = "EVT-CASA-VARAS-2026",
                nombre = "Casa Abierta Sede Antonio Varas",
                tipo = TipoEvento.CASA_ABIERTA,
                sedeAsociada = "Antonio Varas (Providencia)",
                ubicacion = "Antonio Varas 666, Providencia",
                fechaInicio = ahoraEpochMs + unDiaMs,
                fechaFin = ahoraEpochMs + 3 * unDiaMs,
                metaProspectos = 60,
                activo = true,
                archivado = false
            ),
            Evento(
                id = "EVT-EXPO-VALPO-2026",
                nombre = "Expo Educación Superior Valparaíso",
                tipo = TipoEvento.FERIA,
                sedeAsociada = "Valparaíso",
                ubicacion = "Terminal de Pasajeros Valparaíso",
                fechaInicio = ahoraEpochMs + 5 * unDiaMs,
                fechaFin = ahoraEpochMs + 6 * unDiaMs,
                metaProspectos = 50,
                activo = false,
                archivado = false
            ),
            Evento(
                id = "EVT-LICEO-BARRA-2025",
                nombre = "Feria Liceo Eduardo de la Barra 2025",
                tipo = TipoEvento.COLEGIO,
                sedeAsociada = "Valparaíso",
                ubicacion = "Liceo Eduardo de la Barra, Valparaíso",
                fechaInicio = ahoraEpochMs - 90 * unDiaMs,
                fechaFin = ahoraEpochMs - 89 * unDiaMs,
                metaProspectos = 20,
                activo = false,
                archivado = true
            )
        )
    }

    /**
     * Genera un RUT chileno formateado con dígito verificador válido Módulo 11 a partir de su cuerpo numérico.
     */
    fun validRut(cuerpo: String): String {
        val digits = cuerpo.filter(Char::isDigit)
        require(digits.length in 7..8) { "Cuerpo de RUT inválido: $cuerpo" }
        val dv = RutUtils.calculateDv(digits)
        return RutUtils.formatRut("$digits$dv")
    }

    /**
     * Genera un conjunto realista de prospectos demo distribuidos entre captadores,
     * sedes, niveles de interés, rangos etarios y estados de sincronización.
     */
    fun generarProspectosDemo(
        ahoraEpochMs: Long = System.currentTimeMillis(),
        eventoId: String = DEFAULT_EVENT_ID
    ): List<Prospecto> {
        val haceDosHoras = ahoraEpochMs - 2 * 60 * 60 * 1000L
        val haceCuatroHoras = ahoraEpochMs - 4 * 60 * 60 * 1000L
        val ayer = ahoraEpochMs - 24 * 60 * 60 * 1000L

        return listOf(
            Prospecto(
                id = "demo-uuid-1",
                captadorId = 2,
                eventoId = eventoId,
                fechaCaptura = ahoraEpochMs - 15 * 60 * 1000L,
                rut = validRut("20123456"),
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
                carreraPrioridad2 = "Analista Programador Computacional",
                syncStatus = SyncStatus.PENDING_SYNC
            ),
            Prospecto(
                id = "demo-uuid-2",
                captadorId = 2,
                eventoId = eventoId,
                fechaCaptura = ahoraEpochMs - 45 * 60 * 1000L,
                rut = validRut("11111111"),
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
                carreraPrioridad2 = "Administración Hotelera",
                syncStatus = SyncStatus.PENDING_SYNC
            ),
            Prospecto(
                id = "demo-uuid-3",
                captadorId = 2,
                eventoId = eventoId,
                fechaCaptura = haceDosHoras,
                rut = validRut("21456789"),
                nombres = "Valentina Paz",
                apellidos = "Rojas Muñoz",
                genero = Genero.FEMENINO,
                fechaNacimiento = "10/03/2009",
                comunaResidencia = "Quilpué",
                celular = "+56991234567",
                email = "valentina.rojas@duocmail.cl",
                consentimientoDatos = true,
                situacionAcademica = SituacionAcademica.CUARTO_MEDIO,
                establecimientoEducacional = "Colegio Aconcagua",
                comunaEstablecimiento = "Quilpué",
                jornadaPreferencia = Jornada.DIURNA,
                sedeInteres = "Viña del Mar",
                nivelInteres = NivelInteres.ALTO,
                perfilMotivacional = PerfilMotivacional.VOCACIONAL,
                carreraPrioridad1 = "Diseño Gráfico",
                carreraPrioridad2 = "Animación Digital",
                carreraPrioridad3 = "Publicidad",
                syncStatus = SyncStatus.SYNCED
            ),
            Prospecto(
                id = "demo-uuid-4",
                captadorId = 2,
                eventoId = eventoId,
                fechaCaptura = haceCuatroHoras,
                rut = validRut("19876543"),
                nombres = "Matías Nicolás",
                apellidos = "Contreras Díaz",
                genero = Genero.MASCULINO,
                fechaNacimiento = "04/11/2003",
                comunaResidencia = "Providencia",
                celular = "+56976543210",
                email = "matias.contreras@correo.cl",
                consentimientoDatos = true,
                situacionAcademica = SituacionAcademica.OTRA,
                situacionAcademicaOtro = "Trabajador",
                establecimientoEducacional = "Liceo Lastarria",
                comunaEstablecimiento = "Providencia",
                jornadaPreferencia = Jornada.VESPERTINA,
                sedeInteres = "Antonio Varas",
                nivelInteres = NivelInteres.ALTO,
                perfilMotivacional = PerfilMotivacional.INVERSION,
                carreraPrioridad1 = "Ingeniería en Conectividad y Redes",
                carreraPrioridad2 = "Ingeniería en Informática",
                syncStatus = SyncStatus.SYNCED
            ),
            Prospecto(
                id = "demo-uuid-5",
                captadorId = 2,
                eventoId = eventoId,
                fechaCaptura = ayer,
                rut = validRut("18654321"),
                nombres = "Sofía Belén",
                apellidos = "Araya Fuentes",
                genero = Genero.FEMENINO,
                fechaNacimiento = "18/07/1998",
                comunaResidencia = "Maipú",
                celular = "+56965432109",
                email = "sofia.araya@gmail.com",
                consentimientoDatos = true,
                situacionAcademica = SituacionAcademica.OTRA,
                situacionAcademicaOtro = "Educación superior incompleta",
                establecimientoEducacional = "Colegio Santa María",
                comunaEstablecimiento = "Maipú",
                jornadaPreferencia = Jornada.VESPERTINA,
                sedeInteres = "Plaza Oeste",
                nivelInteres = NivelInteres.MEDIO,
                perfilMotivacional = PerfilMotivacional.REINGRESO,
                carreraPrioridad1 = "Auditoría",
                carreraPrioridad2 = "Ingeniería en Administración",
                syncStatus = SyncStatus.SYNC_ERROR
            ),
            Prospecto(
                id = "demo-uuid-6",
                captadorId = 4,
                eventoId = eventoId,
                fechaCaptura = ayer - 3600_000L,
                rut = validRut("17234567"),
                nombres = "Diego Alejandro",
                apellidos = "Vega Paredes",
                genero = Genero.MASCULINO,
                fechaNacimiento = "29/01/1990",
                comunaResidencia = "San Joaquín",
                celular = "+56954321098",
                email = "diego.vega@gmail.com",
                consentimientoDatos = true,
                situacionAcademica = SituacionAcademica.OTRA,
                situacionAcademicaOtro = "Titulado técnico",
                establecimientoEducacional = "Liceo Industrial",
                comunaEstablecimiento = "San Joaquín",
                jornadaPreferencia = Jornada.VESPERTINA,
                sedeInteres = "San Joaquín",
                nivelInteres = NivelInteres.BAJO,
                perfilMotivacional = PerfilMotivacional.ECONOMICO,
                carreraPrioridad1 = "Técnico en Mecánica Automotriz y Autotrónica",
                syncStatus = SyncStatus.SYNCED
            ),
            Prospecto(
                id = "demo-uuid-7",
                captadorId = 4,
                eventoId = eventoId,
                fechaCaptura = ahoraEpochMs - 30 * 60 * 1000L,
                rut = validRut("22345678"),
                nombres = "Antonia Francisca",
                apellidos = "Herrera Núñez",
                genero = Genero.NO_INDICA,
                fechaNacimiento = "12/06/2008",
                comunaResidencia = "Concón",
                celular = "+56943210987",
                email = "antonia.herrera@gmail.com",
                consentimientoDatos = true,
                situacionAcademica = SituacionAcademica.CUARTO_MEDIO,
                establecimientoEducacional = "Colegio Altazor",
                comunaEstablecimiento = "Concón",
                jornadaPreferencia = Jornada.DIURNA,
                sedeInteres = "Viña del Mar",
                nivelInteres = NivelInteres.ALTO,
                perfilMotivacional = PerfilMotivacional.VOCACIONAL,
                carreraPrioridad1 = "Técnico en Enfermería",
                carreraPrioridad2 = "Preparador Físico",
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )
    }
}
