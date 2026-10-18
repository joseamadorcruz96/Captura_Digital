package com.therionlabs.captura_digital_v2.ui.formulario

import androidx.lifecycle.ViewModel
import com.therionlabs.captura_digital_v2.data.model.Genero
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.data.model.SyncStatus
import com.therionlabs.captura_digital_v2.data.repository.ProspectoRepository
import com.therionlabs.captura_digital_v2.util.RutUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FormularioViewModel : ViewModel() {

    private val repository = ProspectoRepository()

    private val _uiState = MutableStateFlow(FormularioUiState())
    val uiState: StateFlow<FormularioUiState> = _uiState.asStateFlow()

    fun onConsentimientoChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(
            consentimiento = value,
            consentimientoError = null
        )
    }

    fun onRutChange(value: String) {
        // Filtrar caracteres válidos para un RUT (dígitos, k, K, puntos, guion)
        val filtered = value.filter { it.isDigit() || it == 'k' || it == 'K' || it == '.' || it == '-' }
        val formatted = RutUtils.formatRut(filtered)
        val cleaned = RutUtils.cleanRut(filtered)

        val error = when {
            cleaned.length >= 8 && !RutUtils.isValidRut(cleaned) -> "RUT inválido (revise dígito verificador)"
            else -> null
        }

        _uiState.value = _uiState.value.copy(
            rut = formatted,
            rutError = error
        )
    }

    fun onNombresChange(value: String) {
        _uiState.value = _uiState.value.copy(nombres = value, nombresError = null)
    }

    fun onApellidosChange(value: String) {
        _uiState.value = _uiState.value.copy(apellidos = value, apellidosError = null)
    }

    fun onGeneroChange(value: Genero) {
        _uiState.value = _uiState.value.copy(genero = value)
    }

    fun onFechaNacimientoChange(value: String) {
        _uiState.value = _uiState.value.copy(fechaNacimiento = value)
    }

    fun onComunaResidenciaChange(value: String) {
        _uiState.value = _uiState.value.copy(comunaResidencia = value, comunaResidenciaError = null)
    }

    fun onCelularChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _uiState.value = _uiState.value.copy(celular = digitsOnly, celularError = null)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value.trim(), emailError = null)
    }

    fun onSituacionAcademicaChange(value: SituacionAcademica) {
        _uiState.value = _uiState.value.copy(situacionAcademica = value)
    }

    fun onSituacionAcademicaOtroChange(value: String) {
        _uiState.value = _uiState.value.copy(situacionAcademicaOtro = value)
    }

    fun onEstablecimientoChange(value: String) {
        _uiState.value = _uiState.value.copy(establecimientoEducacional = value, establecimientoError = null)
    }

    fun onComunaEstablecimientoChange(value: String) {
        _uiState.value = _uiState.value.copy(comunaEstablecimiento = value)
    }

    fun onJornadaChange(value: Jornada) {
        _uiState.value = _uiState.value.copy(jornada = value)
    }

    fun onSedeChange(value: String) {
        _uiState.value = _uiState.value.copy(sedeInteres = value)
    }

    fun onNivelInteresChange(value: NivelInteres) {
        _uiState.value = _uiState.value.copy(nivelInteres = value)
    }

    fun onPerfilMotivacionalChange(value: PerfilMotivacional) {
        _uiState.value = _uiState.value.copy(perfilMotivacional = value)
    }

    fun onEscuelaSeleccionadaChange(escuelaId: String) {
        _uiState.value = _uiState.value.copy(escuelaSeleccionadaId = escuelaId)
    }

    fun onCarrera1Change(value: String) {
        _uiState.value = _uiState.value.copy(carreraPrioridad1 = value, carrera1Error = null)
    }

    fun onCarrera2Change(value: String) {
        _uiState.value = _uiState.value.copy(carreraPrioridad2 = value)
    }

    fun onCarrera3Change(value: String) {
        _uiState.value = _uiState.value.copy(carreraPrioridad3 = value)
    }

    fun guardar(captadorId: Int, eventoId: String?) {
        val state = _uiState.value

        var hasError = false
        var consentimientoError: String? = null
        var rutError: String? = null
        var nombresError: String? = null
        var apellidosError: String? = null
        var comunaError: String? = null
        var emailError: String? = null
        var celularError: String? = null
        var establecimientoError: String? = null
        var carrera1Error: String? = null

        // 1. Consentimiento
        if (!state.consentimiento) {
            consentimientoError = "Debe aceptar el consentimiento para continuar"
            hasError = true
        }

        // 2. RUT Módulo 11
        if (state.rut.isBlank() || !RutUtils.isValidRut(state.rut)) {
            rutError = "Ingrese un RUT chileno válido (ej: 12.345.678-5)"
            hasError = true
        }

        // 3. Nombres y Apellidos
        if (state.nombres.isBlank()) {
            nombresError = "El nombre es obligatorio"
            hasError = true
        }
        if (state.apellidos.isBlank()) {
            apellidosError = "Los apellidos son obligatorios"
            hasError = true
        }

        // 4. Comuna
        if (state.comunaResidencia.isBlank()) {
            comunaError = "La comuna de residencia es obligatoria"
            hasError = true
        }

        // 5. Canales de contacto
        val emailValido = state.email.isNotBlank() && state.email.contains("@") && state.email.contains(".")
        val celularValido = state.celular.length in 8..9

        if (state.email.isBlank() && state.celular.isBlank()) {
            emailError = "Debe ingresar al menos un canal de contacto (Email o Celular)"
            celularError = "Debe ingresar al menos un canal de contacto (Email o Celular)"
            hasError = true
        } else {
            if (state.email.isNotBlank() && !emailValido) {
                emailError = "Formato de correo electrónico inválido"
                hasError = true
            }
            if (state.celular.isNotBlank() && !celularValido) {
                celularError = "Ingrese entre 8 y 9 dígitos"
                hasError = true
            }
        }

        // 6. Establecimiento
        if (state.establecimientoEducacional.isBlank()) {
            establecimientoError = "El colegio o liceo de origen es obligatorio"
            hasError = true
        }

        // 7. Carrera Prioridad 1
        if (state.carreraPrioridad1.isBlank()) {
            carrera1Error = "Seleccione al menos una carrera como primera preferencia"
            hasError = true
        }

        if (hasError) {
            _uiState.value = state.copy(
                consentimientoError = consentimientoError,
                rutError = rutError,
                nombresError = nombresError,
                apellidosError = apellidosError,
                comunaResidenciaError = comunaError,
                emailError = emailError,
                celularError = celularError,
                establecimientoError = establecimientoError,
                carrera1Error = carrera1Error
            )
            return
        }

        // Crear modelo de dominio Prospecto
        val prospecto = Prospecto(
            captadorId = captadorId,
            eventoId = eventoId,
            consentimientoDatos = true,
            rut = state.rut,
            nombres = state.nombres.trim(),
            apellidos = state.apellidos.trim(),
            genero = state.genero,
            fechaNacimiento = state.fechaNacimiento.ifBlank { null },
            comunaResidencia = state.comunaResidencia.trim(),
            celular = if (state.celular.isNotBlank()) "+569${state.celular}" else "",
            email = state.email.trim(),
            situacionAcademica = state.situacionAcademica,
            situacionAcademicaOtro = state.situacionAcademicaOtro.ifBlank { null },
            establecimientoEducacional = state.establecimientoEducacional.trim(),
            comunaEstablecimiento = state.comunaEstablecimiento.ifBlank { state.comunaResidencia.trim() },
            jornadaPreferencia = state.jornada,
            sedeInteres = state.sedeInteres,
            nivelInteres = state.nivelInteres,
            perfilMotivacional = state.perfilMotivacional,
            carreraPrioridad1 = state.carreraPrioridad1,
            carreraPrioridad2 = state.carreraPrioridad2.ifBlank { null },
            carreraPrioridad3 = state.carreraPrioridad3.ifBlank { null },
            syncStatus = SyncStatus.PENDING_SYNC
        )

        repository.guardarProspecto(prospecto)

        _uiState.value = state.copy(
            isSaving = false,
            saveSuccess = true,
            savedProspectoNombre = prospecto.nombreCompleto,
            savedProspectoRut = prospecto.rut
        )
    }

    fun resetFormulario() {
        _uiState.value = FormularioUiState(
            sedeInteres = _uiState.value.sedeInteres,
            escuelaSeleccionadaId = _uiState.value.escuelaSeleccionadaId
        )
    }
}
