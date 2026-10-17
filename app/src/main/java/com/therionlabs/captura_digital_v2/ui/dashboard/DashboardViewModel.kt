package com.therionlabs.captura_digital_v2.ui.dashboard

import androidx.lifecycle.ViewModel
import com.therionlabs.captura_digital_v2.data.model.Genero
import com.therionlabs.captura_digital_v2.data.model.UserRole
import com.therionlabs.captura_digital_v2.data.repository.AuthRepository
import com.therionlabs.captura_digital_v2.data.repository.ProspectoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

class DashboardViewModel : ViewModel() {

    private val prospectoRepository = ProspectoRepository()
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {

        val user = authRepository.getCurrentUser()
        val prospectos = if (user.role == UserRole.ADMINISTRATIVO) {
            prospectoRepository.getAll()
        } else {
            prospectoRepository.getByCaptador(user.id)
        }

        val ages = prospectos.mapNotNull { calcularEdad(it.fechaNacimiento) }

        val byGender = Genero.entries.associate { genero ->
            genero.displayName to prospectos.count { it.genero == genero }
        }

        val byAge = linkedMapOf(
            "Menor de 18" to ages.count { it < 18 },
            "18 a 24" to ages.count { it in 18..24 },
            "25 a 34" to ages.count { it in 25..34 },
            "35 o más" to ages.count { it >= 35 }
        )

        val byInterest = prospectos
            .flatMap { listOfNotNull(it.carreraPrioridad1, it.carreraPrioridad2, it.carreraPrioridad3) }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .toMap()

        _uiState.value = DashboardUiState(
            total = prospectos.size,
            averageAge = if (ages.isEmpty()) 0 else ages.average().toInt(),
            byGender = byGender,
            byAge = byAge,
            byInterest = byInterest
        )
    }

    private fun calcularEdad(fecha: String?): Int? {
        val year = fecha?.substringAfterLast("/")?.toIntOrNull() ?: return null
        return Calendar.getInstance().get(Calendar.YEAR) - year
    }
}