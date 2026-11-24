package com.therionlabs.captura_digital_v2.ui.formularioModal

import com.therionlabs.captura_digital_v2.data.local.EstablecimientoItem
import com.therionlabs.captura_digital_v2.data.model.FormularioBorrador
import com.therionlabs.captura_digital_v2.data.model.Genero
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.data.model.SyncStatus

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
 * Contrato de estado inmutable para el flujo Stepper 2.0 y Modo Express del Cupón de Admisión.
 */
data class StepFormUiState(
    // Modo de captura: Completo (6 pasos) vs Express (Fast-Track)
    val isExpressMode: Boolean = false,

    // Paso 1: Consentimiento Legal (Ley N° 19.628 / 21.719) + Firma Digital
    val consentimiento: Boolean = false,
    val consentimientoError: String? = null,
    val firmaDigitalBase64: String? = null,
    val firmaTimestampEpoch: Long? = null,
    val firmaError: String? = null,

    // Paso 2: Datos Personales + Escáner / Detección de duplicado
    val rut: String = "",
    val rutError: String? = null,
    val nombres: String = "",
    val nombresError: String? = null,
    val apellidos: String = "",
    val apellidosError: String? = null,
    val genero: Genero = Genero.NO_INDICA,
    val fechaNacimiento: String = "",
    val scannerFeedbackMessage: String? = null,
    val duplicateProspecto: Prospecto? = null,
    val showDuplicateDetailDialog: Boolean = false,
    val editingProspectoId: String? = null,

    // Paso 3: Contacto y Residencia + Autocompletado Comuna
    val comunaResidencia: String = "",
    val comunaResidenciaError: String? = null,
    val comunaResidenciaSuggestions: List<String> = emptyList(),
    val celular: String = "",
    val celularError: String? = null,
    val email: String = "",
    val emailError: String? = null,

    // Paso 4: Antecedentes Escolares + Autocompletado Establecimiento y Comuna
    val situacionAcademica: SituacionAcademica = SituacionAcademica.CUARTO_MEDIO,
    val situacionAcademicaOtro: String = "",
    val establecimientoEducacional: String = "",
    val establecimientoError: String? = null,
    val establecimientoSuggestions: List<EstablecimientoItem> = emptyList(),
    val comunaEstablecimiento: String = "",
    val comunaEstablecimientoSuggestions: List<String> = emptyList(),
    val jornada: Jornada = Jornada.DIURNA,

    // Paso 5: Sede y Perfilamiento + Geolocalización opcional
    val sedeInteres: String? = null,
    val sedeError: String? = null,
    val nivelInteres: NivelInteres? = null,
    val nivelInteresError: String? = null,
    val perfilMotivacional: PerfilMotivacional? = null,
    val perfilMotivacionalError: String? = null,
    val attachLocation: Boolean = false,
    val isFetchingLocation: Boolean = false,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val locationStatusMessage: String? = null,

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

    // Borradores locales (no sincronizables)
    val draftId: String? = null,
    val pendingDraftToResume: FormularioBorrador? = null,
    val draftSavedDialogVisible: Boolean = false,
    val lastDraftSavedEpoch: Long? = null,

    // Control de guardado
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val savedProspectoNombre: String = "",
    val savedProspectoRut: String = "",
    val savedSyncStatus: SyncStatus? = null,
    val savedWasExpress: Boolean = false
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

    val isUpdatingExisting: Boolean
        get() = editingProspectoId != null
}
