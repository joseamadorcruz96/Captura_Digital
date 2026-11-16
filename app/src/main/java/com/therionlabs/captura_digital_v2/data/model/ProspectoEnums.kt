package com.therionlabs.captura_digital_v2.data.model


// Enums de negocio para el modelo de Prospecto / Cupón de Admisión Duoc UC.
//Coinciden 1:1 con las opciones declaradas en el cupón físico de 21x21 cm.

//OK
enum class Genero(val displayName: String) {
    FEMENINO("Femenino"),
    MASCULINO("Masculino"),
    NO_INDICA("No indica")
}

enum class SituacionAcademica(val displayName: String) {
    CUARTO_MEDIO("4° Medio"),
    EGRESADO_EM("Egresado Enseñanza Media"),
    OTRA("Otra")
}

enum class Jornada(val displayName: String, val horario: String) {
    DIURNA(
        displayName = "Diurna",
        horario = "Lunes a Viernes de 08:30 a 19:00 hrs"
    ),
    VESPERTINA(
        displayName = "Vespertina",
        horario = "Lunes a Viernes de 19:00 a 22:45 hrs / Sábados de 08:30 a 16:00 hrs"
    )
}

enum class NivelInteres(val displayName: String) {
    ALTO("Interés Alto"),
    MEDIO("Interés Medio"),
    BAJO("Interés Bajo")
}

enum class PerfilMotivacional(
    val titulo: String,
    val descripcion: String
) {
    INVERSION(
        titulo = "Inversión y Progreso Económico",
        descripcion = "Estudiar es una inversión para aumentar mis ingresos y progresar en la vida, tanto yo como mi familia."
    ),
    VOCACIONAL(
        titulo = "Vocacional Puro",
        descripcion = "Estudiar es seguir mi vocación para encontrar mi lugar en el mundo laboral. Los ingresos no son lo central."
    ),
    REINGRESO(
        titulo = "Reingreso / Segunda Oportunidad",
        descripcion = "Estudiar es una nueva oportunidad de retomar esa carrera que dejé pendiente o comenzar otra nueva carrera."
    ),
    ECONOMICO(
        titulo = "Sensibilidad al Financiamiento",
        descripcion = "Estudiar es muy importante, sin embargo, no sé si tendré los ingresos suficientes para financiar mi carrera."
    )
}

enum class SyncStatus(val displayName: String) {
    PENDING_SYNC("Pendiente de sincronizar"),
    SYNCED("Sincronizado"),
    SYNC_ERROR("Error de sincronización")
}
