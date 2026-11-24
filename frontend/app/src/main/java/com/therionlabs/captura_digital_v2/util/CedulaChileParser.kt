package com.therionlabs.captura_digital_v2.util

import java.net.URLDecoder

/**
 * Resultado normalizado extraído desde un código QR o PDF417 de Cédula de Identidad chilena.
 */
data class CedulaScanResult(
    val rut: String,
    val nombres: String? = null,
    val apellidos: String? = null,
    val fechaNacimiento: String? = null,
    val sourceFormat: String = "QR"
)

/**
 * Parser puro (100% testeable en JVM sin dependencias Android) para decodificar payloads de
 * Cédula de Identidad chilena emitidos por el Registro Civil (QR URL y PDF417 / MRZ).
 */
object CedulaChileParser {

    private val RUN_PARAM_REGEX = Regex("""(?:^|[?&])(?:RUN|RUT)=([^&\s]+)""", RegexOption.IGNORE_CASE)
    private val GENERIC_PARAM_REGEX = Regex("""(?:^|[?&])([A-Za-z_]+)=([^&\s]+)""")
    private val RUT_WITH_DASH_REGEX = Regex("""\b(\d{1,2}\.?\d{3}\.?\d{3}-[0-9Kk]|\d{7,8}-[0-9Kk])\b""")
    private val RUT_COMPACT_REGEX = Regex("""(?:^|[^0-9])0?(\d{7,8}[0-9Kk])(?=$|[^0-9A-Za-z])""")
    private val DATE_DDMMYYYY_REGEX = Regex("""\b(\d{2})[/-](\d{2})[/-](\d{4})\b""")

    fun parse(rawPayload: String?): CedulaScanResult? {
        val raw = rawPayload?.trim().orEmpty()
        if (raw.isBlank()) return null

        // 1. Formato URL QR del Registro Civil (ej. https://portal.sidiv.registrocivil.cl/docstatus?RUN=12345678-5&...)
        if (raw.contains("RUN=", ignoreCase = true) || raw.contains("RUT=", ignoreCase = true) ||
            raw.startsWith("http", ignoreCase = true)
        ) {
            parseQrUrl(raw)?.let { return it }
        }

        // 2. Formato PDF417 / MRZ de cédula chilena
        return parsePdf417OrText(raw)
    }

    private fun parseQrUrl(raw: String): CedulaScanResult? {
        val runMatch = RUN_PARAM_REGEX.find(raw)
        val rawRut = runMatch?.groupValues?.getOrNull(1)?.let(::urlDecode)
            ?: extractValidRutFromText(raw)
            ?: return null

        val formattedRut = normalizeValidRut(rawRut) ?: return null

        val params = GENERIC_PARAM_REGEX.findAll(raw).associate { match ->
            match.groupValues[1].uppercase() to urlDecode(match.groupValues[2])
        }

        var nombres = params["NOMBRES"]?.takeIf { it.isNotBlank() }?.let(::toTitleCase)
        var apellidos = params["APELLIDOS"]?.takeIf { it.isNotBlank() }?.let(::toTitleCase)
        val fechaNac = params["FECHANAC"]?.takeIf { it.isNotBlank() }
            ?: params["NACIMIENTO"]?.takeIf { it.isNotBlank() }

        // Algunas URLs QR incluyen el parámetro `mrz` con apellidos<<nombres
        val mrzParam = params["MRZ"]
        if ((nombres == null || apellidos == null) && !mrzParam.isNullOrBlank()) {
            val (mrzApellidos, mrzNombres) = parseMrzNames(mrzParam)
            if (apellidos == null) apellidos = mrzApellidos
            if (nombres == null) nombres = mrzNombres
        }

        return CedulaScanResult(
            rut = formattedRut,
            nombres = nombres,
            apellidos = apellidos,
            fechaNacimiento = fechaNac,
            sourceFormat = "QR_REGISTRO_CIVIL"
        )
    }

    private fun parsePdf417OrText(raw: String): CedulaScanResult? {
        val formattedRut = extractValidRutFromText(raw) ?: return null

        var nombres: String? = null
        var apellidos: String? = null
        var fechaNac: String? = null

        // Si contiene estructura MRZ con `<<` (ej. VALENZUELA<SOTO<<CAMILA<ANDREA)
        if (raw.contains("<<")) {
            val (mrzApellidos, mrzNombres) = parseMrzNames(raw)
            apellidos = mrzApellidos
            nombres = mrzNombres
        }

        // Si viene delimitado por pipes, saltos de línea o tabulaciones (formato típico PDF417)
        if (nombres == null || apellidos == null) {
            val tokens = raw.split('|', '\n', '\r', '\t', ';')
                .map { it.trim() }
                .filter { it.isNotBlank() }

            // Extraer fecha si aparece en formato DD/MM/AAAA o DD-MM-AAAA
            fechaNac = tokens.firstNotNullOfOrNull { token ->
                DATE_DDMMYYYY_REGEX.find(token)?.let { m ->
                    "${m.groupValues[1]}/${m.groupValues[2]}/${m.groupValues[3]}"
                }
            }

            // Buscar pares o bloques etiquetados o puramente alfabéticos
            val labeledApellidos = findLabeledValue(tokens, listOf("APELLIDOS", "APELLIDO"))
            val labeledNombres = findLabeledValue(tokens, listOf("NOMBRES", "NOMBRE"))
            if (labeledApellidos != null) apellidos = toTitleCase(labeledApellidos)
            if (labeledNombres != null) nombres = toTitleCase(labeledNombres)

            if (apellidos == null || nombres == null) {
                val nameCandidates = tokens.filter { token ->
                    val cleaned = token.trim()
                    cleaned.length >= 2 &&
                        !cleaned.equals("CHL", ignoreCase = true) &&
                        !cleaned.equals("CHILE", ignoreCase = true) &&
                        !cleaned.equals("CEDULA", ignoreCase = true) &&
                        !cleaned.startsWith("http", ignoreCase = true) &&
                        !cleaned.contains("=") &&
                        cleaned.none { it.isDigit() } &&
                        cleaned.all { it.isLetter() || it == ' ' || it == '\'' || it == '-' }
                }

                when {
                    nameCandidates.size >= 4 -> {
                        // Orden habitual PDF417: Apellido1 | Apellido2 | Nombre1 | Nombre2
                        apellidos = toTitleCase("${nameCandidates[0]} ${nameCandidates[1]}")
                        nombres = toTitleCase(nameCandidates.drop(2).take(2).joinToString(" "))
                    }
                    nameCandidates.size == 3 -> {
                        // Apellido1 | Apellido2 | Nombres  o  Apellidos | Nombre1 | Nombre2
                        apellidos = toTitleCase("${nameCandidates[0]} ${nameCandidates[1]}")
                        nombres = toTitleCase(nameCandidates[2])
                    }
                    nameCandidates.size == 2 -> {
                        apellidos = toTitleCase(nameCandidates[0])
                        nombres = toTitleCase(nameCandidates[1])
                    }
                }
            }
        }

        return CedulaScanResult(
            rut = formattedRut,
            nombres = nombres,
            apellidos = apellidos,
            fechaNacimiento = fechaNac,
            sourceFormat = "PDF417"
        )
    }

    private fun findLabeledValue(tokens: List<String>, prefixes: List<String>): String? {
        for (token in tokens) {
            val upper = token.uppercase()
            for (prefix in prefixes) {
                if (upper.startsWith("$prefix:") || upper.startsWith("$prefix=")) {
                    val value = token.substringAfter(':').substringAfter('=').trim()
                    if (value.isNotBlank()) return value
                }
            }
        }
        return null
    }

    private fun parseMrzNames(mrzText: String): Pair<String?, String?> {
        val mrzLine = mrzText
            .split('\n', '\r', '|', ';')
            .firstOrNull { it.contains("<<") }
            ?: mrzText

        val parts = mrzLine.split("<<", limit = 2)
        if (parts.size < 2) return null to null

        val rawApellidos = parts[0]
            .replace('<', ' ')
            .trim()
            .replace(Regex("\\s+"), " ")
        val rawNombres = parts[1]
            .substringBefore("<<")
            .replace('<', ' ')
            .trim()
            .replace(Regex("\\s+"), " ")

        val apellidos = rawApellidos
            .takeIf { it.length >= 2 && it.all { ch -> ch.isLetter() || ch == ' ' } }
            ?.let(::toTitleCase)
        val nombres = rawNombres
            .takeIf { it.length >= 2 && it.all { ch -> ch.isLetter() || ch == ' ' } }
            ?.let(::toTitleCase)

        return apellidos to nombres
    }

    private fun extractValidRutFromText(text: String): String? {
        // 1) Buscar primero RUTs con guion (más confiables)
        RUT_WITH_DASH_REGEX.findAll(text).forEach { match ->
            normalizeValidRut(match.groupValues[1])?.let { return it }
        }
        // 2) Buscar bloques compactos de 7-8 dígitos + DV
        RUT_COMPACT_REGEX.findAll(text).forEach { match ->
            normalizeValidRut(match.groupValues[1])?.let { return it }
        }
        return null
    }

    private fun normalizeValidRut(rawRut: String): String? {
        var cleaned = RutUtils.cleanRut(rawRut)
        if (cleaned.length == 10 && cleaned.startsWith("0")) {
            cleaned = cleaned.drop(1)
        }
        return if (RutUtils.isValidRut(cleaned)) {
            RutUtils.formatRut(cleaned)
        } else {
            null
        }
    }

    private fun urlDecode(value: String): String =
        runCatching { URLDecoder.decode(value, "UTF-8") }.getOrDefault(value)

    private fun toTitleCase(input: String): String {
        return input.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { ch -> ch.titlecase() }
            }
    }
}
