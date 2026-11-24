package com.therionlabs.captura_digital_v2.data.local

import android.content.Context
import com.google.gson.Gson
import java.text.Normalizer

data class EstablecimientoItem(
    val nombre: String,
    val comuna: String,
    val region: String = ""
)

private data class RegionComunasJson(
    val region: String = "",
    val comunas: List<String> = emptyList()
)

private data class ComunasCatalogJson(
    val regiones: List<RegionComunasJson> = emptyList()
)

private data class EstablecimientosCatalogJson(
    val establecimientos: List<EstablecimientoItem> = emptyList()
)

/**
 * Fuente de datos local 100% offline para autocompletado de comunas y establecimientos
 * educacionales de Chile. Lee los JSON en `assets/` con caché en memoria y ofrece un
 * fallback seguro en memoria para tests unitarios JVM y `@Preview`.
 */
class CatalogoLocalidadesDataSource(
    private val context: Context? = null,
    comunasOverride: List<String>? = null,
    establecimientosOverride: List<EstablecimientoItem>? = null
) {
    private val comunasCache: List<String> by lazy {
        comunasOverride ?: loadComunasFromAssetsOrFallback()
    }

    private val establecimientosCache: List<EstablecimientoItem> by lazy {
        establecimientosOverride ?: loadEstablecimientosFromAssetsOrFallback()
    }

    fun getTodasComunas(): List<String> = comunasCache

    fun getTodosEstablecimientos(): List<EstablecimientoItem> = establecimientosCache

    /**
     * Busca comunas cuyo nombre contenga la consulta (sin distinción de tildes ni mayúsculas),
     * priorizando las que comienzan con el prefijo escrito.
     */
    fun buscarComunas(query: String, limit: Int = 8): List<String> {
        val normQuery = normalizar(query)
        if (normQuery.isBlank()) return emptyList()

        return comunasCache
            .mapNotNull { comuna ->
                val normComuna = normalizar(comuna)
                when {
                    normComuna == normQuery -> 0 to comuna
                    normComuna.startsWith(normQuery) -> 1 to comuna
                    normComuna.contains(normQuery) -> 2 to comuna
                    else -> null
                }
            }
            .sortedWith(compareBy<Pair<Int, String>> { it.first }.thenBy { it.second })
            .map { it.second }
            .distinct()
            .take(limit)
    }

    /**
     * Busca establecimientos por nombre (y opcionalmente prioriza los ubicados en [comunaFiltro]).
     * Permite siempre ingreso de texto libre si el usuario escribe un colegio fuera del catálogo.
     */
    fun buscarEstablecimientos(
        query: String,
        comunaFiltro: String? = null,
        limit: Int = 8
    ): List<EstablecimientoItem> {
        val normQuery = normalizar(query)
        if (normQuery.isBlank()) return emptyList()
        val normComunaFiltro = comunaFiltro?.let(::normalizar)?.takeIf { it.isNotBlank() }

        return establecimientosCache
            .mapNotNull { item ->
                val normNombre = normalizar(item.nombre)
                val normComunaItem = normalizar(item.comuna)
                val matchesName = normNombre.contains(normQuery)
                val matchesComuna = normComunaItem.contains(normQuery)
                if (!matchesName && !matchesComuna) return@mapNotNull null

                val sameComunaPenalty = if (normComunaFiltro != null && normComunaItem == normComunaFiltro) 0 else 10
                val textRank = when {
                    normNombre == normQuery -> 0
                    normNombre.startsWith(normQuery) -> 1
                    matchesName -> 2
                    else -> 5
                }
                (sameComunaPenalty + textRank) to item
            }
            .sortedWith(compareBy<Pair<Int, EstablecimientoItem>> { it.first }.thenBy { it.second.nombre })
            .map { it.second }
            .distinctBy { it.nombre to it.comuna }
            .take(limit)
    }

    private fun loadComunasFromAssetsOrFallback(): List<String> {
        val raw = readAssetTextOrNull("comunas_chile.json") ?: return FALLBACK_COMUNAS
        return parseComunasJson(raw).ifEmpty { FALLBACK_COMUNAS }
    }

    private fun loadEstablecimientosFromAssetsOrFallback(): List<EstablecimientoItem> {
        val raw = readAssetTextOrNull("establecimientos_chile.json") ?: return FALLBACK_ESTABLECIMIENTOS
        return parseEstablecimientosJson(raw).ifEmpty { FALLBACK_ESTABLECIMIENTOS }
    }

    private fun readAssetTextOrNull(fileName: String): String? {
        val appContext = context?.applicationContext ?: return null
        return runCatching {
            appContext.assets.open(fileName).bufferedReader(Charsets.UTF_8).use { it.readText() }
        }.getOrNull()
    }

    companion object {
        private val DIACRITICS_REGEX = Regex("\\p{InCombiningDiacriticalMarks}+")
        private val gson = Gson()

        fun normalizar(texto: String): String {
            val nfd = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
            return DIACRITICS_REGEX.replace(nfd, "").lowercase()
        }

        fun parseComunasJson(json: String): List<String> {
            return runCatching {
                val parsed = gson.fromJson(json, ComunasCatalogJson::class.java)
                parsed?.regiones
                    ?.flatMap { it.comunas }
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?.distinct()
                    ?.sorted()
                    .orEmpty()
            }.getOrDefault(emptyList())
        }

        fun parseEstablecimientosJson(json: String): List<EstablecimientoItem> {
            return runCatching {
                val parsed = gson.fromJson(json, EstablecimientosCatalogJson::class.java)
                parsed?.establecimientos
                    ?.filter { it.nombre.isNotBlank() }
                    ?.distinctBy { it.nombre to it.comuna }
                    .orEmpty()
            }.getOrDefault(emptyList())
        }

        val FALLBACK_COMUNAS: List<String> = listOf(
            "Algarrobo", "Alto Hospicio", "Ancud", "Angol", "Antofagasta", "Arauco", "Arica",
            "Buin", "Calama", "Caldera", "Casablanca", "Castro", "Cauquenes", "Cerrillos",
            "Cerro Navia", "Chiguayante", "Chillán", "Colina", "Concepción", "Conchalí", "Concón",
            "Copiapó", "Coquimbo", "Coronel", "Curicó", "El Bosque", "Estación Central", "Frutillar",
            "Huechuraba", "Independencia", "Iquique", "La Cisterna", "La Cruz", "La Florida",
            "La Granja", "La Pintana", "La Reina", "La Serena", "Las Condes", "Limache", "Linares",
            "Lo Barnechea", "Lo Espejo", "Lo Prado", "Los Andes", "Los Ángeles", "Macul", "Maipú",
            "Melipilla", "Nacimiento", "Ñuñoa", "Osorno", "Ovalle", "Padre Hurtado", "Pedro Aguirre Cerda",
            "Peñalolén", "Peñaflor", "Providencia", "Pudahuel", "Puente Alto", "Puerto Montt",
            "Puerto Varas", "Punta Arenas", "Quilicura", "Quillota", "Quilpué", "Quinta Normal",
            "Rancagua", "Recoleta", "Renca", "San Antonio", "San Bernardo", "San Felipe", "San Joaquín",
            "San Miguel", "San Pedro de la Paz", "San Ramón", "Santiago", "Talagante", "Talca",
            "Talcahuano", "Temuco", "Valdivia", "Valparaíso", "Villa Alemana", "Villarrica",
            "Viña del Mar", "Vitacura"
        )

        val FALLBACK_ESTABLECIMIENTOS: List<EstablecimientoItem> = listOf(
            EstablecimientoItem("Colegio Rubén Castro", "Viña del Mar", "Valparaíso"),
            EstablecimientoItem("Colegio Aconcagua", "Quilpué", "Valparaíso"),
            EstablecimientoItem("Colegio Altazor", "Concón", "Valparaíso"),
            EstablecimientoItem("Liceo Eduardo de la Barra", "Valparaíso", "Valparaíso"),
            EstablecimientoItem("Liceo Lastarria", "Providencia", "Metropolitana de Santiago"),
            EstablecimientoItem("Liceo José Victorino Lastarria", "Providencia", "Metropolitana de Santiago"),
            EstablecimientoItem("Colegio Santa María", "Maipú", "Metropolitana de Santiago"),
            EstablecimientoItem("Liceo Industrial", "San Joaquín", "Metropolitana de Santiago"),
            EstablecimientoItem("Instituto Nacional General José Miguel Carrera", "Santiago", "Metropolitana de Santiago"),
            EstablecimientoItem("Liceo Bicentenario de Viña del Mar", "Viña del Mar", "Valparaíso"),
            EstablecimientoItem("Liceo Enrique Molina Garmendia", "Concepción", "Biobío"),
            EstablecimientoItem("Liceo Comercial Miramar", "Puerto Montt", "Los Lagos"),
            EstablecimientoItem("Liceo Politécnico Villarrica", "Villarrica", "La Araucanía")
        )
    }
}
