package com.therionlabs.captura_digital_v2.ui.formularioModal

import com.therionlabs.captura_digital_v2.data.model.Genero
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica

/**
 * Etapas del formulario modal Stepper (6 pasos).
 */
enum class FormStep(
    val index: Int,
    val title: String,
    val shortTitle: String
) {
    CONSENTIMIENTO(0, "Consentimiento Legal", "Legal"),
    DATOS_PERSONALES(1, "Datos Personales", "Datos"),
    CONTACTO(2, "Contacto y Residencia", "Contacto"),
    ANTECEDENTES(3, "Antecedentes Escolares", "Colegio"),
    SEDE_PERFIL(4, "Sede y Perfilamiento", "Sede"),
    CARRERAS(5, "Preferencias de Carrera", "Carreras")
}

/**
 * Estado individual de cada etapa en el Stepper.
 */
data class StepStatus(
    val step: FormStep,
    val isComplete: Boolean = false,
    val hasAlert: Boolean = false,
    val isVisited: Boolean = false
)

/**
 * Contrato de estado inmutable para el flujo Stepper del Cupón de Admisión.
 */
data class StepFormUiState(
    // Paso 1: Consentimiento Legal (Ley N° 19.628 / 21.719)
    val consentimiento: Boolean = false,
    val consentimientoError: String? = null,

    // Paso 2: Datos Personales
    val rut: String = "",
    val rutError: String? = null,
    val nombres: String = "",
    val nombresError: String? = null,
    val apellidos: String = "",
    val apellidosError: String? = null,
    val genero: Genero = Genero.NO_INDICA,
    val fechaNacimiento: String = "",

    // Paso 3: Contacto y Residencia
    val comunaResidencia: String = "",
    val comunaResidenciaError: String? = null,
    val celular: String = "",
    val celularError: String? = null,
    val email: String = "",
    val emailError: String? = null,

    // Paso 4: Antecedentes Escolares y Jornada
    val situacionAcademica: SituacionAcademica = SituacionAcademica.CUARTO_MEDIO,
    val situacionAcademicaOtro: String = "",
    val establecimientoEducacional: String = "",
    val establecimientoError: String? = null,
    val comunaEstablecimiento: String = "",
    val jornada: Jornada = Jornada.DIURNA,

    // Paso 5: Sede y Perfilamiento
    val sedeInteres: String = "Viña del Mar",
    val nivelInteres: NivelInteres = NivelInteres.ALTO,
    val perfilMotivacional: PerfilMotivacional = PerfilMotivacional.VOCACIONAL,

    // Paso 6: Selección de Carreras (1 a 3 prioridades)
    val escuelaSeleccionadaId: String = "INFOR",
    val carreraPrioridad1: String = "",
    val carrera1Error: String? = null,
    val carreraPrioridad2: String = "",
    val carreraPrioridad3: String = "",

    // Estado del Stepper
    val currentStep: FormStep = FormStep.CONSENTIMIENTO,
    val stepStatuses: List<StepStatus> = FormStep.entries.map {
        StepStatus(
            step = it,
            isVisited = it == FormStep.CONSENTIMIENTO
        )
    },
    val generalErrorMessage: String? = null,

    // Control de guardado
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val savedProspectoNombre: String = "",
    val savedProspectoRut: String = ""
) {
    val totalSteps: Int
        get() = FormStep.entries.size

    val completedStepsCount: Int
        get() = stepStatuses.count { it.isComplete }

    val progress: Float
        get() = completedStepsCount.toFloat() / totalSteps.toFloat()

    val isFirstStep: Boolean
        get() = currentStep.index == 0

    val isLastStep: Boolean
        get() = currentStep.index == totalSteps - 1
}
