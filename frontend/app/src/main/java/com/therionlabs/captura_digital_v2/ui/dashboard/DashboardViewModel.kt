package com.therionlabs.captura_digital_v2.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.therionlabs.captura_digital_v2.data.model.Genero
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.UserRole
import com.therionlabs.captura_digital_v2.data.remote.IndicadoresApi
import com.therionlabs.captura_digital_v2.data.remote.RetrofitClient
import com.therionlabs.captura_digital_v2.data.repository.AuthRepository
import com.therionlabs.captura_digital_v2.data.repository.IProspectoRepository
import com.therionlabs.captura_digital_v2.data.repository.ProspectoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardViewModel(
    private val prospectoRepository: IProspectoRepository = ProspectoRepository(),
    private val authRepository: AuthRepository = AuthRepository(),
    private val indicadoresApi: IndicadoresApi = RetrofitClient.indicadoresApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        load()
        refrescarRemoto()
        cargarIndicadores()
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

        _uiState.value = _uiState.value.copy(
            isAdmin = user.role == UserRole.ADMINISTRATIVO,
            total = prospectos.size,
            averageAge = if (ages.isEmpty()) 0 else ages.average().toInt(),
            byGender = byGender,
            byAge = byAge,
            byInterest = byInterest,
            prospectos = prospectos
        )
    }

    private fun refrescarRemoto() {
        val user = authRepository.getCurrentUser()
        val filtroCaptador = if (user.role == UserRole.ADMINISTRATIVO) null else user.id
        viewModelScope.launch {
            if (prospectoRepository.refrescarDesdeServidor(filtroCaptador)) {
                load()
            }
        }
    }

    fun rotarNivelInteres(prospecto: Prospecto) {
        val siguiente = when (prospecto.nivelInteres) {
            NivelInteres.ALTO -> NivelInteres.MEDIO
            NivelInteres.MEDIO -> NivelInteres.BAJO
            NivelInteres.BAJO -> NivelInteres.ALTO
        }
        viewModelScope.launch {
            val ok = prospectoRepository.actualizarProspecto(prospecto.copy(nivelInteres = siguiente))
            load()
            _uiState.value = _uiState.value.copy(
                actionMessage = if (ok) {
                    "Actualizado ${prospecto.nombres} a ${siguiente.displayName}"
                } else {
                    "No se pudo actualizar el prospecto"
                }
            )
        }
    }

    fun eliminarProspecto(clientUuid: String) {
        viewModelScope.launch {
            val ok = prospectoRepository.eliminarProspecto(clientUuid)
            load()
            _uiState.value = _uiState.value.copy(
                actionMessage = if (ok) "Prospecto eliminado" else "No se pudo eliminar"
            )
        }
    }

    private fun cargarIndicadores() {
        viewModelScope.launch {
            try {
                val indicadores = indicadoresApi.obtener()
                _uiState.value = _uiState.value.copy(
                    uf = indicadores.uf.valor,
                    dolar = indicadores.dolar.valor,
                    utm = indicadores.utm.valor
                )
            } catch (_: Exception) {
            }
        }
    }

    fun shareText(): String {
        val state = _uiState.value
        return buildString {
            appendLine("Resumen Captura Digital")
            appendLine()
            appendLine("Encuestas: ${state.total}")
            appendLine("Edad promedio: ${state.averageAge} años")
            appendLine()
            appendLine("Género:")
            state.byGender.forEach { (label, count) -> appendLine("- $label: $count") }
            appendLine()
            appendLine("Rango de edad:")
            state.byAge.forEach { (label, count) -> appendLine("- $label: $count") }
            appendLine()
            appendLine("Carreras de interés:")
            state.byInterest.forEach { (label, count) -> appendLine("- $label: $count") }
        }
    }

    private fun calcularEdad(fecha: String?): Int? {
        val year = fecha?.substringAfterLast("/")?.toIntOrNull() ?: return null
        return Calendar.getInstance().get(Calendar.YEAR) - year
    }
}
