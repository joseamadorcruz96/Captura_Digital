package com.therionlabs.captura_digital_v2.ui.formularioModal

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.therionlabs.captura_digital_v2.data.model.Genero
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.ui.components.DuocButton
import com.therionlabs.captura_digital_v2.ui.components.DuocHeader
import com.therionlabs.captura_digital_v2.ui.components.DuocOutlinedButton
import com.therionlabs.captura_digital_v2.ui.formularioModal.components.StepProgressBar
import com.therionlabs.captura_digital_v2.ui.formularioModal.steps.AntecedentesStepScreen
import com.therionlabs.captura_digital_v2.ui.formularioModal.steps.CarrerasStepScreen
import com.therionlabs.captura_digital_v2.ui.formularioModal.steps.ConsentimientoStepScreen
import com.therionlabs.captura_digital_v2.ui.formularioModal.steps.ContactoStepScreen
import com.therionlabs.captura_digital_v2.ui.formularioModal.steps.DatosPersonalesStepScreen
import com.therionlabs.captura_digital_v2.ui.formularioModal.steps.SedePerfilamientoStepScreen
import com.therionlabs.captura_digital_v2.ui.theme.Captura_Digital_v2Theme

@Composable
fun StepFormRoute(
    viewModel: StepFormViewModel = viewModel(),
    captadorId: Int = 2,
    eventoId: String? = "EVT-DUOC-2026",
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StepFormScreen(
        uiState = uiState,
        onStepSelected = viewModel::goToStep,
        onNextStep = viewModel::nextStep,
        onPreviousStep = viewModel::previousStep,
        onConsentimientoChange = viewModel::onConsentimientoChange,
        onRutChange = viewModel::onRutChange,
        onNombresChange = viewModel::onNombresChange,
        onApellidosChange = viewModel::onApellidosChange,
        onGeneroChange = viewModel::onGeneroChange,
        onFechaNacimientoChange = viewModel::onFechaNacimientoChange,
        onComunaResidenciaChange = viewModel::onComunaResidenciaChange,
        onCelularChange = viewModel::onCelularChange,
        onEmailChange = viewModel::onEmailChange,
        onSituacionAcademicaChange = viewModel::onSituacionAcademicaChange,
        onSituacionAcademicaOtroChange = viewModel::onSituacionAcademicaOtroChange,
        onEstablecimientoChange = viewModel::onEstablecimientoChange,
        onComunaEstablecimientoChange = viewModel::onComunaEstablecimientoChange,
        onJornadaChange = viewModel::onJornadaChange,
        onSedeChange = viewModel::onSedeChange,
        onNivelInteresChange = viewModel::onNivelInteresChange,
        onPerfilMotivacionalChange = viewModel::onPerfilMotivacionalChange,
        onEscuelaSeleccionadaChange = viewModel::onEscuelaSeleccionadaChange,
        onCarrera1Change = viewModel::onCarrera1Change,
        onCarrera2Change = viewModel::onCarrera2Change,
        onCarrera3Change = viewModel::onCarrera3Change,
        onGuardar = { viewModel.guardar(captadorId, eventoId) },
        onReset = viewModel::resetFormulario,
        onBack = onBack
    )
}

@Composable
fun StepFormScreen(
    uiState: StepFormUiState,
    onStepSelected: (FormStep) -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onConsentimientoChange: (Boolean) -> Unit,
    onRutChange: (String) -> Unit,
    onNombresChange: (String) -> Unit,
    onApellidosChange: (String) -> Unit,
    onGeneroChange: (Genero) -> Unit,
    onFechaNacimientoChange: (String) -> Unit,
    onComunaResidenciaChange: (String) -> Unit,
    onCelularChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSituacionAcademicaChange: (SituacionAcademica) -> Unit,
    onSituacionAcademicaOtroChange: (String) -> Unit,
    onEstablecimientoChange: (String) -> Unit,
    onComunaEstablecimientoChange: (String) -> Unit,
    onJornadaChange: (Jornada) -> Unit,
    onSedeChange: (String) -> Unit,
    onNivelInteresChange: (NivelInteres) -> Unit,
    onPerfilMotivacionalChange: (PerfilMotivacional) -> Unit,
    onEscuelaSeleccionadaChange: (String) -> Unit,
    onCarrera1Change: (String) -> Unit,
    onCarrera2Change: (String) -> Unit,
    onCarrera3Change: (String) -> Unit,
    onGuardar: () -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DuocHeader(
            title = "Cupón de Admisión",
            subtitle = "Captura digital por etapas • Duoc UC",
            onBack = onBack
        )

        StepProgressBar(
            currentStep = uiState.currentStep,
            stepStatuses = uiState.stepStatuses,
            progress = uiState.progress,
            onStepSelected = onStepSelected
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "stepScreenTransition"
            ) { step ->
                when (step) {
                    FormStep.CONSENTIMIENTO -> ConsentimientoStepScreen(
                        consentimiento = uiState.consentimiento,
                        consentimientoError = uiState.consentimientoError,
                        onConsentimientoChange = onConsentimientoChange
                    )

                    FormStep.DATOS_PERSONALES -> DatosPersonalesStepScreen(
                        rut = uiState.rut,
                        rutError = uiState.rutError,
                        nombres = uiState.nombres,
                        nombresError = uiState.nombresError,
                        apellidos = uiState.apellidos,
                        apellidosError = uiState.apellidosError,
                        genero = uiState.genero,
                        fechaNacimiento = uiState.fechaNacimiento,
                        onRutChange = onRutChange,
                        onNombresChange = onNombresChange,
                        onApellidosChange = onApellidosChange,
                        onGeneroChange = onGeneroChange,
                        onFechaNacimientoChange = onFechaNacimientoChange
                    )

                    FormStep.CONTACTO -> ContactoStepScreen(
                        comunaResidencia = uiState.comunaResidencia,
                        comunaResidenciaError = uiState.comunaResidenciaError,
                        celular = uiState.celular,
                        celularError = uiState.celularError,
                        email = uiState.email,
                        emailError = uiState.emailError,
                        onComunaResidenciaChange = onComunaResidenciaChange,
                        onCelularChange = onCelularChange,
                        onEmailChange = onEmailChange
                    )

                    FormStep.ANTECEDENTES -> AntecedentesStepScreen(
                        situacionAcademica = uiState.situacionAcademica,
                        situacionAcademicaOtro = uiState.situacionAcademicaOtro,
                        establecimientoEducacional = uiState.establecimientoEducacional,
                        establecimientoError = uiState.establecimientoError,
                        comunaEstablecimiento = uiState.comunaEstablecimiento,
                        jornada = uiState.jornada,
                        onSituacionAcademicaChange = onSituacionAcademicaChange,
                        onSituacionAcademicaOtroChange = onSituacionAcademicaOtroChange,
                        onEstablecimientoChange = onEstablecimientoChange,
                        onComunaEstablecimientoChange = onComunaEstablecimientoChange,
                        onJornadaChange = onJornadaChange
                    )

                    FormStep.SEDE_PERFIL -> SedePerfilamientoStepScreen(
                        sedeInteres = uiState.sedeInteres,
                        nivelInteres = uiState.nivelInteres,
                        perfilMotivacional = uiState.perfilMotivacional,
                        onSedeChange = onSedeChange,
                        onNivelInteresChange = onNivelInteresChange,
                        onPerfilMotivacionalChange = onPerfilMotivacionalChange
                    )

                    FormStep.CARRERAS -> CarrerasStepScreen(
                        escuelaSeleccionadaId = uiState.escuelaSeleccionadaId,
                        carreraPrioridad1 = uiState.carreraPrioridad1,
                        carrera1Error = uiState.carrera1Error,
                        carreraPrioridad2 = uiState.carreraPrioridad2,
                        carreraPrioridad3 = uiState.carreraPrioridad3,
                        onEscuelaSeleccionadaChange = onEscuelaSeleccionadaChange,
                        onCarrera1Change = onCarrera1Change,
                        onCarrera2Change = onCarrera2Change,
                        onCarrera3Change = onCarrera3Change
                    )
                }
            }

            if (uiState.generalErrorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.generalErrorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        // Barra inferior fija de navegación entre pasos
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!uiState.isFirstStep) {
                    DuocOutlinedButton(
                        text = "Anterior",
                        onClick = onPreviousStep,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (!uiState.isLastStep) {
                    DuocButton(
                        text = "Siguiente",
                        onClick = onNextStep,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    DuocButton(
                        text = "Guardar Prospecto",
                        onClick = onGuardar,
                        isLoading = uiState.isSaving,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (uiState.saveSuccess) {
        AlertDialog(
            onDismissRequest = onReset,
            confirmButton = {
                DuocButton(
                    text = "Capturar Siguiente",
                    onClick = onReset
                )
            },
            dismissButton = {
                TextButton(onClick = onBack) {
                    Text("Volver al Inicio", color = MaterialTheme.colorScheme.primary)
                }
            },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "¡Prospecto Registrado!",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column {
                    Text("Los datos fueron validados y almacenados localmente de forma segura en modo Offline.")
                    Spacer(Modifier.height(8.dp))
                    Text("• Postulante: ${uiState.savedProspectoNombre}", fontWeight = FontWeight.SemiBold)
                    Text("• RUT validado: ${uiState.savedProspectoRut}", fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun StepFormScreenPreviewContent(uiState: StepFormUiState) {
    StepFormScreen(
        uiState = uiState,
        onStepSelected = {},
        onNextStep = {},
        onPreviousStep = {},
        onConsentimientoChange = {},
        onRutChange = {},
        onNombresChange = {},
        onApellidosChange = {},
        onGeneroChange = {},
        onFechaNacimientoChange = {},
        onComunaResidenciaChange = {},
        onCelularChange = {},
        onEmailChange = {},
        onSituacionAcademicaChange = {},
        onSituacionAcademicaOtroChange = {},
        onEstablecimientoChange = {},
        onComunaEstablecimientoChange = {},
        onJornadaChange = {},
        onSedeChange = {},
        onNivelInteresChange = {},
        onPerfilMotivacionalChange = {},
        onEscuelaSeleccionadaChange = {},
        onCarrera1Change = {},
        onCarrera2Change = {},
        onCarrera3Change = {},
        onGuardar = {},
        onReset = {},
        onBack = {}
    )
}

@Preview(name = "StepFormScreen - Paso 2 (Claro)", showBackground = true, showSystemUi = true)
@Composable
fun StepFormScreenPreview() {
    val statuses = listOf(
        StepStatus(FormStep.CONSENTIMIENTO, isComplete = true, hasAlert = false, isVisited = true),
        StepStatus(FormStep.DATOS_PERSONALES, isComplete = false, hasAlert = false, isVisited = true),
        StepStatus(FormStep.CONTACTO, isComplete = false, hasAlert = true, isVisited = true),
        StepStatus(FormStep.ANTECEDENTES, isComplete = false, hasAlert = false, isVisited = false),
        StepStatus(FormStep.SEDE_PERFIL, isComplete = false, hasAlert = false, isVisited = false),
        StepStatus(FormStep.CARRERAS, isComplete = false, hasAlert = false, isVisited = false)
    )
    Captura_Digital_v2Theme {
        StepFormScreenPreviewContent(
            uiState = StepFormUiState(
                consentimiento = true,
                rut = "20.123.456-5",
                nombres = "Camila Andrea",
                apellidos = "Valenzuela Soto",
                currentStep = FormStep.DATOS_PERSONALES,
                stepStatuses = statuses
            )
        )
    }
}

@Preview(name = "StepFormScreen - Paso Final Guardado", showBackground = true, showSystemUi = true)
@Composable
fun StepFormScreenSuccessDialogPreview() {
    val statuses = FormStep.entries.map {
        StepStatus(it, isComplete = true, hasAlert = false, isVisited = true)
    }
    Captura_Digital_v2Theme {
        StepFormScreenPreviewContent(
            uiState = StepFormUiState(
                currentStep = FormStep.CARRERAS,
                stepStatuses = statuses,
                carreraPrioridad1 = "Ingeniería en Informática",
                saveSuccess = true,
                savedProspectoNombre = "Camila Andrea Valenzuela Soto",
                savedProspectoRut = "20.123.456-5"
            )
        )
    }
}

@Preview(name = "StepFormScreen - Oscuro", showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StepFormScreenDarkPreview() {
    val statuses = listOf(
        StepStatus(FormStep.CONSENTIMIENTO, isComplete = true, hasAlert = false, isVisited = true),
        StepStatus(FormStep.DATOS_PERSONALES, isComplete = true, hasAlert = false, isVisited = true),
        StepStatus(FormStep.CONTACTO, isComplete = false, hasAlert = false, isVisited = true),
        StepStatus(FormStep.ANTECEDENTES, isComplete = false, hasAlert = false, isVisited = false),
        StepStatus(FormStep.SEDE_PERFIL, isComplete = false, hasAlert = false, isVisited = false),
        StepStatus(FormStep.CARRERAS, isComplete = false, hasAlert = false, isVisited = false)
    )
    Captura_Digital_v2Theme {
        StepFormScreenPreviewContent(
            uiState = StepFormUiState(
                currentStep = FormStep.CONTACTO,
                stepStatuses = statuses,
                comunaResidencia = "Viña del Mar",
                celular = "98765432",
                email = "camila@duoc.cl",
                generalErrorMessage = "Completa o corrige las siguientes etapas antes de guardar: Contacto y Residencia"
            )
        )
    }
}
