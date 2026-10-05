package com.therionlabs.captura_digital_v2.ui.dashboard

data class DashboardUiState(
    val total: Int = 0,
    val averageAge: Int = 0,
    val byGender: Map<String, Int> = emptyMap(),
    val byAge: Map<String, Int> = emptyMap(),
    val byInterest: Map<String, Int> = emptyMap()
)