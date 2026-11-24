package com.therionlabs.captura_digital_v2.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Utilidades deterministas de formateo y parsing de fechas y duraciones para Eventos y Turnos.
 * Compatible con minSdk = 24 sin requerir desugaring de java.time.
 */
object EventoDateUtils {

    private val displayFormatter: SimpleDateFormat
        get() = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-CL")).apply {
            isLenient = false
        }

    private val isoFormatter: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            isLenient = false
        }

    private val timeFormatter: SimpleDateFormat
        get() = SimpleDateFormat("HH:mm", Locale.forLanguageTag("es-CL"))

    fun formatDisplayDate(epochMs: Long): String = displayFormatter.format(epochMs)

    fun formatIsoDate(epochMs: Long): String = isoFormatter.format(epochMs)

    fun formatTime(epochMs: Long): String = timeFormatter.format(epochMs)

    fun formatDateRange(fechaInicio: Long, fechaFin: Long): String {
        val start = formatDisplayDate(fechaInicio)
        val end = formatDisplayDate(fechaFin)
        return if (start == end) start else "$start – $end"
    }

    fun formatDuration(duracionMs: Long): String {
        val totalSeconds = (duracionMs / 1000L).coerceAtLeast(0L)
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }

    /**
     * Parsea fechas en formato `dd/MM/yyyy` o `yyyy-MM-dd` al inicio del día (00:00:00) o fin del día (23:59:59).
     */
    fun parseDate(raw: String, endOfDay: Boolean = false): Long? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null

        val date = runCatching { displayFormatter.parse(trimmed) }.getOrNull()
            ?: runCatching { isoFormatter.parse(trimmed) }.getOrNull()
            ?: return null

        return Calendar.getInstance().apply {
            time = date
            if (endOfDay) {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            } else {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }.timeInMillis
    }
}
