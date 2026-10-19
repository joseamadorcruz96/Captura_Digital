package com.therionlabs.captura_digital_v2.util

/**
 * Utilidades para la validación y formateo de RUT chileno
 * utilizando el algoritmo estándar de Módulo 11.
 */
object RutUtils {

    /**
     * Limpia un RUT eliminando puntos, guiones y espacios en blanco,
     * convirtiendo la letra 'k' a mayúscula.
     */
    fun cleanRut(rut: String): String {
        return rut.replace(".", "")
            .replace("-", "")
            .trim()
            .uppercase()
    }

    /**
     * Valida si un RUT chileno es válido según el algoritmo Módulo 11.
     * Soporta formatos con o sin puntos y guión (8 a 9 caracteres limpios).
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
     * Calcula el dígito verificador para el cuerpo de un RUT dado usando Módulo 11.
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
     * Formatea un texto a formato estándar de RUT chileno: XX.XXX.XXX-X o X.XXX.XXX-X.
     * Si el texto tiene menos de 8 caracteres limpios, se devuelve limpio sin agregar
     * caracteres prematuramente.
     */
    fun formatRut(input: String): String {
        val cleaned = cleanRut(input)
        if (cleaned.length < 8) return cleaned

        val takeClean = cleaned.take(9)
        val cuerpo = takeClean.substring(0, takeClean.length - 1)
        val dv = takeClean.last()

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
