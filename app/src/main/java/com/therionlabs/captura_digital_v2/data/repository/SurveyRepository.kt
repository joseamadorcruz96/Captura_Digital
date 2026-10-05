package com.therionlabs.captura_digital_v2.data.repository

import com.therionlabs.captura_digital_v2.data.model.Survey

class SurveyRepository {

    // Datos de ejemplo: se reemplazarán por las encuestas reales del formulario
    private val surveys = listOf(
        Survey(1, 2, "Femenino", 19, listOf("Tecnología", "Diseño")),
        Survey(2, 2, "Masculino", 22, listOf("Tecnología")),
        Survey(3, 2, "Femenino", 25, listOf("Salud")),
        Survey(4, 2, "Masculino", 18, listOf("Gastronomía", "Turismo")),
        Survey(5, 2, "Femenino", 31, listOf("Administración")),
        Survey(6, 2, "Otro", 20, listOf("Diseño")),
        Survey(7, 2, "Masculino", 27, listOf("Tecnología", "Administración")),
        Survey(8, 2, "Femenino", 17, listOf("Salud", "Turismo")),
        Survey(9, 2, "Masculino", 35, listOf("Construcción")),
        Survey(10, 2, "Femenino", 23, listOf("Tecnología"))
    )

    fun getAll(): List<Survey> = surveys

    fun getByCaptador(captadorId: Int): List<Survey> =
        surveys.filter { it.captadorId == captadorId }
}