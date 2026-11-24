package com.therionlabs.captura_digital_v2

import com.therionlabs.captura_digital_v2.data.local.Converters
import com.therionlabs.captura_digital_v2.data.model.EstadoCompletitud
import com.therionlabs.captura_digital_v2.data.model.Genero
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.data.model.SyncStatus
import com.therionlabs.captura_digital_v2.data.model.dto.toDomain
import com.therionlabs.captura_digital_v2.data.model.dto.toDto
import com.therionlabs.captura_digital_v2.data.repository.ProspectoRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para validar el modelo único Prospecto y su capa de persistencia:
 * 1. Propiedades calculadas de Prospecto y mapeo bidireccional con ProspectoRequestDto.
 * 2. Conversión de Enums de negocio con Converters.
 * 3. Operaciones de repositorio con ProspectoRepository.
 */
class ProspectoPersistenceTest {

    private val converters = Converters()

    @Test
    fun prospecto_toDto_and_back_preservesAllFields() {
        val original = Prospecto(
            id = "test-uuid-1234",
            captadorId = 5,
            eventoId = "EVT-VIÑA-2026",
            fechaCaptura = 1776543210000L,
            syncStatus = SyncStatus.PENDING_SYNC,
            consentimientoDatos = true,
            rut = "19.876.543-2",
            nombres = "Matías Esteban",
            apellidos = "Rojas Palma",
            genero = Genero.MASCULINO,
            fechaNacimiento = "05/11/2007",
            comunaResidencia = "Quilpué",
            celular = "+56998765432",
            email = "matias.rojas@gmail.com",
            situacionAcademica = SituacionAcademica.CUARTO_MEDIO,
            situacionAcademicaOtro = null,
            establecimientoEducacional = "Colegio Aconcagua",
            comunaEstablecimiento = "Quilpué",
            jornadaPreferencia = Jornada.DIURNA,
            sedeInteres = "Viña del Mar",
            nivelInteres = NivelInteres.ALTO,
            perfilMotivacional = PerfilMotivacional.VOCACIONAL,
            carreraPrioridad1 = "Ingeniería en Informática",
            carreraPrioridad2 = "Diseño Digital",
            carreraPrioridad3 = "Animación Digital",
            estadoCompletitud = EstadoCompletitud.EXPRESS,
            firmaDigitalBase64 = "iVBORw0KGgoAAAANSUhEUg==",
            firmaTimestampEpoch = 1776543215000L,
            latitud = -33.0245,
            longitud = -71.5518
        )

        assertEquals("Matías Esteban Rojas Palma", original.nombreCompleto)
        assertEquals(3, original.cantidadCarrerasElegidas)

        val dto = original.toDto()
        assertEquals(original.id, dto.clientUuid)
        assertEquals(original.rut, dto.rut)
        assertEquals(original.nombres, dto.nombres)
        assertEquals(original.apellidos, dto.apellidos)
        assertEquals("MASCULINO", dto.genero)
        assertEquals("DIURNA", dto.jornadaPreferencia)
        assertEquals("EXPRESS", dto.estadoCompletitud)
        assertEquals(original.firmaDigitalBase64, dto.firmaDigitalBase64)
        assertEquals(original.latitud, dto.latitud)
        assertEquals(3, dto.carreras.size)

        val mappedBack = dto.toDomain(syncStatus = SyncStatus.PENDING_SYNC)
        assertEquals(original, mappedBack)

        // Retrocompatibilidad: DTO antiguo sin nuevos campos asume EstadoCompletitud.COMPLETO
        val legacyMapped = dto.copy(
            estadoCompletitud = null,
            firmaDigitalBase64 = null,
            firmaTimestampEpoch = null,
            latitud = null,
            longitud = null
        ).toDomain(syncStatus = SyncStatus.SYNCED)
        assertEquals(EstadoCompletitud.COMPLETO, legacyMapped.estadoCompletitud)
    }

    @Test
    fun converters_convertAllEnumsCorrectly() {
        // Genero
        assertEquals("FEMENINO", converters.fromGenero(Genero.FEMENINO))
        assertEquals(Genero.FEMENINO, converters.toGenero("FEMENINO"))
        assertEquals(Genero.NO_INDICA, converters.toGenero("VALOR_INVALIDO"))

        // SituacionAcademica
        assertEquals("EGRESADO_EM", converters.fromSituacionAcademica(SituacionAcademica.EGRESADO_EM))
        assertEquals(SituacionAcademica.EGRESADO_EM, converters.toSituacionAcademica("EGRESADO_EM"))

        // Jornada
        assertEquals("VESPERTINA", converters.fromJornada(Jornada.VESPERTINA))
        assertEquals(Jornada.VESPERTINA, converters.toJornada("VESPERTINA"))

        // NivelInteres
        assertEquals("ALTO", converters.fromNivelInteres(NivelInteres.ALTO))
        assertEquals(NivelInteres.ALTO, converters.toNivelInteres("ALTO"))

        // PerfilMotivacional
        assertEquals("INVERSION", converters.fromPerfilMotivacional(PerfilMotivacional.INVERSION))
        assertEquals(PerfilMotivacional.INVERSION, converters.toPerfilMotivacional("INVERSION"))

        // SyncStatus
        assertEquals("PENDING_SYNC", converters.fromSyncStatus(SyncStatus.PENDING_SYNC))
        assertEquals(SyncStatus.PENDING_SYNC, converters.toSyncStatus("PENDING_SYNC"))
    }

    @Test
    fun repository_savesAndRetrievesProspectoCorrectly() {
        val repository = ProspectoRepository(dao = null)
        val initialCount = repository.getAll().size

        val nuevoProspecto = Prospecto(
            id = "test-uuid-9999",
            captadorId = 99,
            eventoId = "EVT-TEST",
            rut = "22.333.444-5",
            nombres = "Francisca",
            apellidos = "Pérez",
            comunaResidencia = "Santiago",
            celular = "+56911223344",
            email = "francisca@test.cl",
            situacionAcademica = SituacionAcademica.CUARTO_MEDIO,
            establecimientoEducacional = "Colegio Test",
            comunaEstablecimiento = "Santiago",
            jornadaPreferencia = Jornada.DIURNA,
            sedeInteres = "San Bernardo",
            nivelInteres = NivelInteres.ALTO,
            perfilMotivacional = PerfilMotivacional.VOCACIONAL,
            carreraPrioridad1 = "Ingeniería en Informática"
        )

        val guardado = repository.guardarProspecto(nuevoProspecto)
        assertTrue(guardado)

        val all = repository.getAll()
        assertEquals(initialCount + 1, all.size)
        val found = all.find { it.rut == "22.333.444-5" }
        assertNotNull(found)
        assertEquals("Francisca", found?.nombres)

        val byCaptador = repository.getByCaptador(99)
        assertEquals(1, byCaptador.size)
        assertEquals("22.333.444-5", byCaptador.first().rut)
    }
}
