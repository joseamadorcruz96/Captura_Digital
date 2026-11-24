package com.therionlabs.captura_digital_v2

import com.therionlabs.captura_digital_v2.data.local.CatalogoLocalidadesDataSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CatalogoLocalidadesTest {

    private fun resolveAssetFile(name: String): File {
        val candidates = listOf(
            File("src/main/assets/$name"),
            File("app/src/main/assets/$name"),
            File("frontend/app/src/main/assets/$name")
        )
        return candidates.first { it.exists() }
    }

    @Test
    fun assetsReales_contienenTodasLasComunasDeChileYEstablecimientos() {
        val comunasJson = resolveAssetFile("comunas_chile.json").readText(Charsets.UTF_8)
        val estabJson = resolveAssetFile("establecimientos_chile.json").readText(Charsets.UTF_8)

        val comunas = CatalogoLocalidadesDataSource.parseComunasJson(comunasJson)
        val establecimientos = CatalogoLocalidadesDataSource.parseEstablecimientosJson(estabJson)

        assertEquals(346, comunas.size)
        assertTrue(comunas.contains("Viña del Mar"))
        assertTrue(comunas.contains("Ñuñoa"))
        assertTrue(comunas.contains("Punta Arenas"))
        assertTrue(establecimientos.size >= 50)
        assertTrue(establecimientos.any { it.nombre == "Colegio Rubén Castro" && it.comuna == "Viña del Mar" })
    }

    @Test
    fun buscarComunas_filtraSinTildesNiMayusculas() {
        val dataSource = CatalogoLocalidadesDataSource()
        val resultadosVina = dataSource.buscarComunas("vina")
        val resultadosNunoa = dataSource.buscarComunas("nunoa")

        assertTrue(resultadosVina.contains("Viña del Mar"))
        assertTrue(resultadosNunoa.contains("Ñuñoa"))
        assertTrue(dataSource.buscarComunas("").isEmpty())
    }

    @Test
    fun buscarEstablecimientos_priorizaComunaFiltroYPermiteTextoLibre() {
        val dataSource = CatalogoLocalidadesDataSource()
        val liceos = dataSource.buscarEstablecimientos(query = "liceo", comunaFiltro = "Providencia")

        assertTrue(liceos.isNotEmpty())
        assertEquals("Providencia", liceos.first().comuna)

        val inexistente = dataSource.buscarEstablecimientos(query = "Colegio Inventado XYZ 999")
        assertTrue(inexistente.isEmpty())
    }
}
