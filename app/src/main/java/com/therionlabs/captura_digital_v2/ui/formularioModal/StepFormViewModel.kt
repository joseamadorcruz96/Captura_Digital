package com.therionlabs.captura_digital_v2.ui.formularioModal

import androidx.lifecycle.ViewModel
import com.therionlabs.captura_digital_v2.data.model.Genero
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.data.model.SyncStatus
import com.therionlabs.captura_digital_v2.data.repository.IProspectoRepository
import com.therionlabs.captura_digital_v2.data.repository.ProspectoRepository
import com.therionlabs.captura_digital_v2.util.FormularioValidator
import com.therionlabs.captura_digital_v2.util.RutUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel para el formulario modal Stepper de captura de prospectos.
 * Permite navegación libre entre etapas, marca alertas al abandonar pasos
 * incompletos y bloquea el guardado hasta validar las 6 fases.
 */
class StepFormViewModel(
    private val repository: IProspectoRepository
) : ViewModel() {

    constructor() : this(ProspectoRepository())

    private val _uiState = MutableStateFlow(StepFormUiState())
    val uiState: StateFlow<StepFormUiState> = _uiState.asStateFlow()

    // --- Navegación libre entre pasos ---

    fun nextStep() {
        val currentIndex = _uiState.value.currentStep.index
        if (currentIndex < FormStep.entries.lastIndex) {
            goToStep(FormStep.entries[currentIndex + 1])
        }
    }

    fun previousStep() {
        val currentIndex = _uiState.value.currentStep.index
        if (currentIndex > 0) {
            goToStep(FormStep.entries[currentIndex - 1])
        }
    }

    fun goToStep(targetStep: FormStep) {
        _uiState.update { state ->
            val leavingStep = state.currentStep
            if (leavingStep == targetStep) return@update state

            // Al abandonar un paso, aplicamos validación visible y marcamos alerta si falta algo
            val validatedLeavingState = applyStepValidationErrors(state, leavingStep)
            val updatedStatuses = validatedLeavingState.stepStatuses.map { status ->
                when (status.step) {
                    leavingStep -> {
                        val valid = isStepValid(validatedLeavingState, leavingStep)
                        status.copy(
                            isVisited = true,
                            isComplete = valid,
                            hasAlert = !valid
                        )
                    }
                    targetStep -> {
                        val valid = isStepValid(validatedLeavingState, targetStep)
                        status.copy(
                            isVisited = true,
                            isComplete = valid,
                            hasAlert = if (valid) false else status.hasAlert
                        )
                    }
                    else -> status
                }
            }

            validatedLeavingState.copy(
                currentStep = targetStep,
                stepStatuses = updatedStatuses,
                generalErrorMessage = null
            )
        }
    }

    // --- Eventos de campos ---

    fun onConsentimientoChange(value: Boolean) {
        updateStateAndRefreshStep(FormStep.CONSENTIMIENTO) { state ->
            val error = FormularioValidator.validarConsentimiento(value).errorOrNull()
            state.copy(
                consentimiento = value,
                consentimientoError = error
            )
        }
    }

    fun onRutChange(value: String) {
        updateStateAndRefreshStep(FormStep.DATOS_PERSONALES) { state ->
            if (value.endsWith("-")) {
                val raw = RutUtils.cleanRut(value)
                return@updateStateAndRefreshStep state.copy(
                    rut = raw,
                    rutError = null
                )
            }

            val cleaned = RutUtils.cleanRut(value)
            val validChars = StringBuilder()
            for (i in cleaned.indices) {
                val char = cleaned[i]
                if (char.isDigit()) {
                    validChars.append(char)
                } else if (char == 'K' && i == cleaned.length - 1 && validChars.length >= 7) {
                    validChars.append('K')
                }
            }

            val limited = validChars.toString().take(9)
            val formatted = when {
                limited.length == 9 -> RutUtils.formatRut(limited)
                limited.length == 8 && (RutUtils.isValidRut(limited) || limited.endsWith("K")) ->
                    RutUtils.formatRut(limited)
                else -> limited
            }

            val error = when {
                limited.length == 9 && !RutUtils.isValidRut(limited) ->
                    "RUT inválido (revise dígito verificador)"
                limited.length == 8 && limited.endsWith("K") && !RutUtils.isValidRut(limited) ->
                    "RUT inválido (revise dígito verificador)"
                else -> null
            }

            state.copy(
                rut = formatted,
                rutError = error
            )
        }
    }

    fun onNombresChange(value: String) {
        updateStateAndRefreshStep(FormStep.DATOS_PERSONALES) { state ->
            val error = if (state.nombresError != null) {
                FormularioValidator.validarNombres(value).errorOrNull()
            } else null
            state.copy(nombres = value, nombresError = error)
        }
    }

    fun onApellidosChange(value: String) {
        updateStateAndRefreshStep(FormStep.DATOS_PERSONALES) { state ->
            val error = if (state.apellidosError != null) {
                FormularioValidator.validarApellidos(value).errorOrNull()
            } else null
            state.copy(apellidos = value, apellidosError = error)
        }
    }

    fun onGeneroChange(value: Genero) {
        updateStateAndRefreshStep(FormStep.DATOS_PERSONALES) { state ->
            state.copy(genero = value)
        }
    }

    fun onFechaNacimientoChange(value: String) {
        updateStateAndRefreshStep(FormStep.DATOS_PERSONALES) { state ->
            state.copy(fechaNacimiento = value)
        }
    }

    fun onComunaResidenciaChange(value: String) {
        updateStateAndRefreshStep(FormStep.CONTACTO) { state ->
            val error = if (state.comunaResidenciaError != null) {
                FormularioValidator.validarComunaResidencia(value).errorOrNull()
            } else null
            state.copy(comunaResidencia = value, comunaResidenciaError = error)
        }
    }

    fun onCelularChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        updateStateAndRefreshStep(FormStep.CONTACTO) { state ->
            val error = if (digitsOnly.isNotBlank() && state.celularError != null) {
                FormularioValidator.validarCelular(digitsOnly).errorOrNull()
            } else null
            val clearedEmailError = if (digitsOnly.isNotBlank() && state.email.isBlank()) {
                null
            } else state.emailError
            state.copy(
                celular = digitsOnly,
                celularError = error,
                emailError = clearedEmailError
            )
        }
    }

    fun onEmailChange(value: String) {
        val trimmed = value.trim()
        updateStateAndRefreshStep(FormStep.CONTACTO) { state ->
            val error = if (trimmed.isNotBlank() && state.emailError != null) {
                FormularioValidator.validarEmail(trimmed).errorOrNull()
            } else null
            val clearedCelularError = if (trimmed.isNotBlank() && state.celular.isBlank()) {
                null
            } else state.celularError
            state.copy(
                email = trimmed,
                emailError = error,
                celularError = clearedCelularError
            )
        }
    }

    fun onSituacionAcademicaChange(value: SituacionAcademica) {
        updateStateAndRefreshStep(FormStep.ANTECEDENTES) { state ->
            state.copy(situacionAcademica = value)
        }
    }

    fun onSituacionAcademicaOtroChange(value: String) {
        updateStateAndRefreshStep(FormStep.ANTECEDENTES) { state ->
            state.copy(situacionAcademicaOtro = value)
        }
    }

    fun onEstablecimientoChange(value: String) {
        updateStateAndRefreshStep(FormStep.ANTECEDENTES) { state ->
            val error = if (state.establecimientoError != null) {
                FormularioValidator.validarEstablecimiento(value).errorOrNull()
            } else null
            state.copy(establecimientoEducacional = value, establecimientoError = error)
        }
    }

    fun onComunaEstablecimientoChange(value: String) {
        updateStateAndRefreshStep(FormStep.ANTECEDENTES) { state ->
            state.copy(comunaEstablecimiento = value)
        }
    }

    fun onJornadaChange(value: Jornada) {
        updateStateAndRefreshStep(FormStep.ANTECEDENTES) { state ->
            state.copy(jornada = value)
        }
    }

    fun onSedeChange(value: String) {
        updateStateAndRefreshStep(FormStep.SEDE_PERFIL) { state ->
            state.copy(sedeInteres = value)
        }
    }

    fun onNivelInteresChange(value: NivelInteres) {
        updateStateAndRefreshStep(FormStep.SEDE_PERFIL) { state ->
            state.copy(nivelInteres = value)
        }
    }

    fun onPerfilMotivacionalChange(value: PerfilMotivacional) {
        updateStateAndRefreshStep(FormStep.SEDE_PERFIL) { state ->
            state.copy(perfilMotivacional = value)
        }
    }

    fun onEscuelaSeleccionadaChange(escuelaId: String) {
        _uiState.update { it.copy(escuelaSeleccionadaId = escuelaId) }
    }

    fun onCarrera1Change(value: String) {
        updateStateAndRefreshStep(FormStep.CARRERAS) { state ->
            state.copy(
                carreraPrioridad1 = value,
                carrera1Error = FormularioValidator.validarCarreraPrioridad1(value).errorOrNull()
            )
        }
    }

    fun onCarrera2Change(value: String) {
        updateStateAndRefreshStep(FormStep.CARRERAS) { state ->
            state.copy(carreraPrioridad2 = value)
        }
    }

    fun onCarrera3Change(value: String) {
        updateStateAndRefreshStep(FormStep.CARRERAS) { state ->
            state.copy(carreraPrioridad3 = value)
        }
    }

    // --- Guardado con validación global de las 6 etapas ---

    fun guardar(captadorId: Int, eventoId: String?) {
        val current = _uiState.value

        var validatedState = current
        for (step in FormStep.entries) {
            validatedState = applyStepValidationErrors(validatedState, step)
        }

        val updatedStatuses = FormStep.entries.map { step ->
            val valid = isStepValid(validatedState, step)
            StepStatus(
                step = step,
                isComplete = valid,
                hasAlert = !valid,
                isVisited = true
            )
        }

        val invalidSteps = updatedStatuses.filter { !it.isComplete }.map { it.step }
        if (invalidSteps.isNotEmpty()) {
            val pendingNames = invalidSteps.joinToString(", ") { it.title }
            _uiState.value = validatedState.copy(
                stepStatuses = updatedStatuses,
                generalErrorMessage = "Completa o corrige las siguientes etapas antes de guardar: $pendingNames"
            )
            return
        }

        val prospecto = Prospecto(
            captadorId = captadorId,
            eventoId = eventoId,
            consentimientoDatos = true,
            rut = RutUtils.formatRut(validatedState.rut),
            nombres = validatedState.nombres.trim(),
            apellidos = validatedState.apellidos.trim(),
            genero = validatedState.genero,
            fechaNacimiento = validatedState.fechaNacimiento.ifBlank { null },
            comunaResidencia = validatedState.comunaResidencia.trim(),
            celular = if (validatedState.celular.isNotBlank()) "+569${validatedState.celular}" else "",
            email = validatedState.email.trim(),
            situacionAcademica = validatedState.situacionAcademica,
            situacionAcademicaOtro = validatedState.situacionAcademicaOtro.ifBlank { null },
            establecimientoEducacional = validatedState.establecimientoEducacional.trim(),
            comunaEstablecimiento = validatedState.comunaEstablecimiento.ifBlank {
                validatedState.comunaResidencia.trim()
            },
            jornadaPreferencia = validatedState.jornada,
            sedeInteres = validatedState.sedeInteres,
            nivelInteres = validatedState.nivelInteres,
            perfilMotivacional = validatedState.perfilMotivacional,
            carreraPrioridad1 = validatedState.carreraPrioridad1,
            carreraPrioridad2 = validatedState.carreraPrioridad2.ifBlank { null },
            carreraPrioridad3 = validatedState.carreraPrioridad3.ifBlank { null },
            syncStatus = SyncStatus.PENDING_SYNC
        )

        repository.guardarProspecto(prospecto)

        _uiState.value = validatedState.copy(
            stepStatuses = updatedStatuses,
            generalErrorMessage = null,
            isSaving = false,
            saveSuccess = true,
            savedProspectoNombre = prospecto.nombreCompleto,
            savedProspectoRut = prospecto.rut
        )
    }

    fun resetFormulario() {
        _uiState.update { state ->
            StepFormUiState(
                sedeInteres = state.sedeInteres,
                escuelaSeleccionadaId = state.escuelaSeleccionadaId
            )
        }
    }

    // --- Helpers internos de validación por etapa ---

    private fun updateStateAndRefreshStep(
        step: FormStep,
        transform: (StepFormUiState) -> StepFormUiState
    ) {
        _uiState.update { prev ->
            val next = transform(prev)
            val valid = isStepValid(next, step)
            val updatedStatuses = next.stepStatuses.map { status ->
                if (status.step == step) {
                    status.copy(
                        isVisited = true,
                        isComplete = valid,
                        hasAlert = if (valid) false else status.hasAlert
                    )
                } else {
                    status
                }
            }
            next.copy(
                stepStatuses = updatedStatuses,
                generalErrorMessage = null
            )
        }
    }

    private fun isStepValid(state: StepFormUiState, step: FormStep): Boolean {
        return when (step) {
            FormStep.CONSENTIMIENTO ->
                FormularioValidator.validarConsentimiento(state.consentimiento).isValid

            FormStep.DATOS_PERSONALES ->
                FormularioValidator.validarRut(state.rut).isValid &&
                    FormularioValidator.validarNombres(state.nombres).isValid &&
                    FormularioValidator.validarApellidos(state.apellidos).isValid

            FormStep.CONTACTO ->
                FormularioValidator.validarComunaResidencia(state.comunaResidencia).isValid &&
                    FormularioValidator.validarContacto(state.email, state.celular).isValid

            FormStep.ANTECEDENTES ->
                FormularioValidator.validarEstablecimiento(state.establecimientoEducacional).isValid

            FormStep.SEDE_PERFIL ->
                FormularioValidator.validarSede(state.sedeInteres).isValid

            FormStep.CARRERAS ->
                FormularioValidator.validarCarreraPrioridad1(state.carreraPrioridad1).isValid
        }
    }

    private fun applyStepValidationErrors(
        state: StepFormUiState,
        step: FormStep
    ): StepFormUiState {
        return when (step) {
            FormStep.CONSENTIMIENTO -> state.copy(
                consentimientoError = FormularioValidator
                    .validarConsentimiento(state.consentimiento)
                    .errorOrNull()
            )

            FormStep.DATOS_PERSONALES -> state.copy(
                rutError = FormularioValidator.validarRut(state.rut).errorOrNull(),
                nombresError = FormularioValidator.validarNombres(state.nombres).errorOrNull(),
                apellidosError = FormularioValidator.validarApellidos(state.apellidos).errorOrNull()
            )

            FormStep.CONTACTO -> {
                val contacto = FormularioValidator.validarContacto(state.email, state.celular)
                state.copy(
                    comunaResidenciaError = FormularioValidator
                        .validarComunaResidencia(state.comunaResidencia)
                        .errorOrNull(),
                    emailError = contacto.emailResultado.errorOrNull(),
                    celularError = contacto.celularResultado.errorOrNull()
                )
            }

            FormStep.ANTECEDENTES -> state.copy(
                establecimientoError = FormularioValidator
                    .validarEstablecimiento(state.establecimientoEducacional)
                    .errorOrNull()
            )

            FormStep.SEDE_PERFIL -> state

            FormStep.CARRERAS -> state.copy(
                carrera1Error = FormularioValidator
                    .validarCarreraPrioridad1(state.carreraPrioridad1)
                    .errorOrNull()
            )
        }
    }
}
