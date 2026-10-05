package com.therionlabs.captura_digital_v2.ui.dashboard

import androidx.lifecycle.ViewModel
import com.therionlabs.captura_digital_v2.data.repository.SurveyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DashboardViewModel : ViewModel() {

    private val repository = SurveyRepository()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        val surveys = repository.getAll()

        // Cantidad por género
        val byGender = surveys.groupingBy { it.gender }.eachCount()

        // Cantidad por rango de edad
        val byAge = linkedMapOf(
            "Menor de 18" to surveys.count { it.age < 18 },
            "18 a 24" to surveys.count { it.age in 18..24 },
            "25 a 34" to surveys.count { it.age in 25..34 },
            "35 o más" to surveys.count { it.age >= 35 }
        )

        // Cantidad por interés, de mayor a menor
        val byInterest = surveys
            .flatMap { it.interests }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .toMap()

        val averageAge = if (surveys.isEmpty()) 0 else surveys.map { it.age }.average().toInt()

        _uiState.value = DashboardUiState(
            total = surveys.size,
            averageAge = averageAge,
            byGender = byGender,
            byAge = byAge,
            byInterest = byInterest
        )
    }
}