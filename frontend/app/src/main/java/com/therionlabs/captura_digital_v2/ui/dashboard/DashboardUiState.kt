package com.therionlabs.captura_digital_v2.ui.dashboard

import com.therionlabs.captura_digital_v2.data.model.Prospecto

data class DashboardUiState(
    val isAdmin: Boolean = false,
    val total: Int = 0,
    val averageAge: Int = 0,
    val byGender: Map<String, Int> = emptyMap(),
    val byAge: Map<String, Int> = emptyMap(),
    val byInterest: Map<String, Int> = emptyMap(),
    val prospectos: List<Prospecto> = emptyList(),
    val actionMessage: String? = null,
    val uf: Double? = null,
    val dolar: Double? = null,
    val utm: Double? = null
)
