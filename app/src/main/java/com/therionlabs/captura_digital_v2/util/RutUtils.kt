package com.therionlabs.captura_digital_v2.util

/**
 * Utilidades para la validación y formateo de RUT chileno
 * utilizando el algoritmo estándar de Módulo 11.
 */
object RutUtils {

    /**
     * Limpia un RUT eliminando puntos, guiones y espacios en blanco.
     */
    fun cleanRut(rut: String): String {
        return rut.replace(".", "")
            .replace("-", "")
            .trim()
            .uppercase()
    }

    /**
     * Valida si un RUT chileno es válido según el algoritmo Módulo 11.
     */
    fun isValidRut(rut: String): Boolean {
        val cleaned = cleanRut(rut)
        if (cleaned.length < 8 || cleaned.length > 9) return false

        val cuerpo = cleaned.substring(0, cleaned.length - 1)
        val dv = cleaned.last()

        if (!cuerpo.all { it.isDigit() }) return false

        return calculateDv(cuerpo) == dv
    }

    /**
     * Calcula el dígito verificador para el cuerpo de un RUT dado.
     */
    fun calculateDv(cuerpo: String): Char {
        var sum = 0
        var factor = 2
        for (i in cuerpo.length - 1 downTo 0) {
            val digit = cuerpo[i].digitToInt()
            sum += digit * factor
            factor = if (factor == 7) 2 else factor + 1
        }
        val remainder = sum % 11
        val result = 11 - remainder
        return when (result) {
            11 -> '0'
            10 -> 'K'
            else -> result.digitToChar()
        }
    }

    /**
     * Formatea un texto a formato de RUT chileno: XX.XXX.XXX-X
     */
    fun formatRut(input: String): String {
        val cleaned = cleanRut(input)
        if (cleaned.isEmpty()) return ""
        if (cleaned.length == 1) return cleaned

        val cuerpo = cleaned.substring(0, cleaned.length - 1)
        val dv = cleaned.last()

        val sb = StringBuilder()
        var count = 0
        for (i in cuerpo.length - 1 downTo 0) {
            sb.append(cuerpo[i])
            count++
            if (count == 3 && i > 0) {
                sb.append('.')
                count = 0
            }
        }
        return "${sb.reverse()}-$dv"
    }
}
