package com.therionlabs.captura_digital_v2

import com.therionlabs.captura_digital_v2.data.local.Converters
import com.therionlabs.captura_digital_v2.data.local.mapper.toDomain
import com.therionlabs.captura_digital_v2.data.local.mapper.toEntity
import com.therionlabs.captura_digital_v2.data.model.Genero
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.data.model.SyncStatus
import com.therionlabs.captura_digital_v2.data.repository.ProspectoRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para validar la persistencia local Room:
 * 1. Mapeo bidireccional entre Prospecto y ProspectoEntity.
 * 2. Conversión de Enums de negocio con Converters.
 * 3. Operaciones de repositorio con ProspectoRepository.
 */
class ProspectoEntityMapperTest {

    private val converters = Converters()

    @Test
    fun prospecto_toEntity_and_back_preservesAllFields() {
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
            carreraPrioridad3 = "Animación Digital"
        )

        val entity = original.toEntity()
        assertEquals(original.id, entity.id)
        assertEquals(original.rut, entity.rut)
        assertEquals(original.nombres, entity.nombres)
        assertEquals(original.apellidos, entity.apellidos)
        assertEquals(original.genero, entity.genero)
        assertEquals(original.jornadaPreferencia, entity.jornadaPreferencia)
        assertEquals(original.carreraPrioridad1, entity.carreraPrioridad1)
        assertEquals(original.carreraPrioridad2, entity.carreraPrioridad2)
        assertEquals(original.carreraPrioridad3, entity.carreraPrioridad3)

        val mappedBack = entity.toDomain()
        assertEquals(original, mappedBack)
        assertEquals("Matías Esteban Rojas Palma", mappedBack.nombreCompleto)
        assertEquals(3, mappedBack.cantidadCarrerasElegidas)
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
