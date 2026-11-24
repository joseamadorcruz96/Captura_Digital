package com.therionlabs.captura_digital_v2.ui.formularioModal

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.therionlabs.captura_digital_v2.CapturaDigitalApp
import com.therionlabs.captura_digital_v2.data.local.EstablecimientoItem
import com.therionlabs.captura_digital_v2.data.model.Genero
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.data.model.SyncStatus
import com.therionlabs.captura_digital_v2.data.repository.AuthRepository
import com.therionlabs.captura_digital_v2.di.rememberAppViewModel
import com.therionlabs.captura_digital_v2.ui.components.DuocButton
import com.therionlabs.captura_digital_v2.ui.components.DuocHeader
import com.therionlabs.captura_digital_v2.ui.components.DuocOutlinedButton
import com.therionlabs.captura_digital_v2.ui.formularioModal.components.CedulaScannerSheet
import com.therionlabs.captura_digital_v2.ui.formularioModal.components.ExpressCaptureForm
import com.therionlabs.captura_digital_v2.ui.formularioModal.components.StepProgressBar
import com.therionlabs.captura_digital_v2.ui.formularioModal.steps.AntecedentesStepScreen
import com.therionlabs.captura_digital_v2.ui.formularioModal.steps.CarrerasStepScreen
import com.therionlabs.captura_digital_v2.ui.formularioModal.steps.ConsentimientoStepScreen
import com.therionlabs.captura_digital_v2.ui.formularioModal.steps.ContactoStepScreen
import com.therionlabs.captura_digital_v2.ui.formularioModal.steps.DatosPersonalesStepScreen
import com.therionlabs.captura_digital_v2.ui.formularioModal.steps.SedePerfilamientoStepScreen
import com.therionlabs.captura_digital_v2.ui.theme.Captura_Digital_v2Theme
import com.therionlabs.captura_digital_v2.ui.theme.DuocAmarillo
import com.therionlabs.captura_digital_v2.ui.theme.DuocAzul
import com.therionlabs.captura_digital_v2.util.EventoDateUtils
import com.therionlabs.captura_digital_v2.util.LocationFetchResult
import com.therionlabs.captura_digital_v2.util.LocationHelper
import kotlinx.coroutines.launch

@Composable
fun StepFormRoute(
    viewModel: StepFormViewModel = rememberAppViewModel { container ->
        if (container != null) {
            StepFormViewModel(
                repository = container.prospectoRepository,
                eventoRepository = container.eventoRepository,
                borradorRepository = container.borradorRepository,
                localidadesDataSource = container.catalogoLocalidadesDataSource
            )
        } else {
            StepFormViewModel()
        }
    },
    captadorId: Int = CapturaDigitalApp.containerOrNull()?.authRepository?.getCurrentUser()?.id
        ?: AuthRepository().getCurrentUser().id,
    eventoId: String? = null,
    borradorId: String? = null,
    prospectoId: String? = null,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showScannerSheet by remember { mutableStateOf(false) }
    var permissionDeniedBanner by remember { mutableStateOf<String?>(null) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            permissionDeniedBanner = null
            showScannerSheet = true
        } else {
            permissionDeniedBanner = "Permiso de cámara denegado. Puedes ingresar el RUT manualmente."
        }
    }

    val fetchLocationNow: () -> Unit = {
        viewModel.onAttachLocationStarted()
        scope.launch {
            when (val res = LocationHelper.fetchApproximateLocation(context)) {
                is LocationFetchResult.Success -> viewModel.onLocationObtained(res.latitud, res.longitud)
                is LocationFetchResult.PermissionDenied ->
                    viewModel.onLocationUnavailable("Permiso de ubicación denegado; puedes guardar igualmente.")
                is LocationFetchResult.ProviderDisabled ->
                    viewModel.onLocationUnavailable("GPS/Ubicación apagados; puedes guardar sin coordenadas.")
                is LocationFetchResult.Unavailable ->
                    viewModel.onLocationUnavailable(res.reason)
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants.values.any { it }
        if (granted) {
            fetchLocationNow()
        } else {
            viewModel.onLocationUnavailable("Permiso de ubicación denegado; puedes guardar igualmente.")
        }
    }

    LaunchedEffect(captadorId, eventoId, borradorId, prospectoId) {
        viewModel.inicializarCaptura(
            captadorId = captadorId,
            eventoId = eventoId,
            borradorId = borradorId,
            prospectoId = prospectoId
        )
    }

    if (showScannerSheet) {
        CedulaScannerSheet(
            onCedulaDetected = { result ->
                showScannerSheet = false
                viewModel.onCedulaScanned(result)
            },
            onDismiss = { showScannerSheet = false }
        )
    }

    StepFormScreen(
        uiState = uiState,
        permissionBannerMessage = permissionDeniedBanner,
        onToggleExpressMode = viewModel::toggleExpressMode,
        onStepSelected = viewModel::goToStep,
        onNextStep = viewModel::nextStep,
        onPreviousStep = viewModel::previousStep,
        onConsentimientoChange = viewModel::onConsentimientoChange,
        onFirmaChange = viewModel::onFirmaChange,
        onScanCedulaClick = {
            val hasCamera = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
            if (hasCamera) {
                permissionDeniedBanner = null
                showScannerSheet = true
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        },
        onRutChange = viewModel::onRutChange,
        onNombresChange = viewModel::onNombresChange,
        onApellidosChange = viewModel::onApellidosChange,
        onGeneroChange = viewModel::onGeneroChange,
        onFechaNacimientoChange = viewModel::onFechaNacimientoChange,
        onComunaResidenciaChange = viewModel::onComunaResidenciaChange,
        onSelectComunaResidencia = viewModel::onSelectComunaResidencia,
        onCelularChange = viewModel::onCelularChange,
        onEmailChange = viewModel::onEmailChange,
        onSituacionAcademicaChange = viewModel::onSituacionAcademicaChange,
        onSituacionAcademicaOtroChange = viewModel::onSituacionAcademicaOtroChange,
        onEstablecimientoChange = viewModel::onEstablecimientoChange,
        onSelectEstablecimiento = viewModel::onSelectEstablecimiento,
        onComunaEstablecimientoChange = viewModel::onComunaEstablecimientoChange,
        onSelectComunaEstablecimiento = viewModel::onSelectComunaEstablecimiento,
        onJornadaChange = viewModel::onJornadaChange,
        onSedeChange = viewModel::onSedeChange,
        onNivelInteresChange = viewModel::onNivelInteresChange,
        onPerfilMotivacionalChange = viewModel::onPerfilMotivacionalChange,
        onToggleAttachLocation = { enabled ->
            if (!enabled) {
                viewModel.onLocationCleared()
            } else if (LocationHelper.hasLocationPermission(context)) {
                fetchLocationNow()
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        },
        onEscuelaSeleccionadaChange = viewModel::onEscuelaSeleccionadaChange,
        onCarrera1Change = viewModel::onCarrera1Change,
        onCarrera2Change = viewModel::onCarrera2Change,
        onCarrera3Change = viewModel::onCarrera3Change,
        onVerFichaDuplicada = viewModel::abrirFichaDuplicada,
        onCerrarFichaDuplicada = viewModel::cerrarFichaDuplicada,
        onCargarDuplicadoParaActualizar = viewModel::cargarDuplicadoParaActualizar,
        onContinuarBorrador = viewModel::continuarBorradorPendiente,
        onEmpezarNuevoBorrador = viewModel::empezarCapturaNuevaManteniendoBorradores,
        onDescartarBorrador = viewModel::descartarBorradorPendiente,
        onGuardarBorrador = { viewModel.guardarComoBorradorExplicito(captadorId, eventoId) },
        onCerrarDialogoBorrador = viewModel::cerrarDialogoBorradorGuardado,
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
    onBack: () -> Unit,
    permissionBannerMessage: String? = null,
    onToggleExpressMode: (Boolean) -> Unit = {},
    onFirmaChange: (String?) -> Unit = {},
    onScanCedulaClick: () -> Unit = {},
    onSelectComunaResidencia: (String) -> Unit = onComunaResidenciaChange,
    onSelectEstablecimiento: (EstablecimientoItem) -> Unit = { onEstablecimientoChange(it.nombre) },
    onSelectComunaEstablecimiento: (String) -> Unit = onComunaEstablecimientoChange,
    onToggleAttachLocation: (Boolean) -> Unit = {},
    onVerFichaDuplicada: () -> Unit = {},
    onCerrarFichaDuplicada: () -> Unit = {},
    onCargarDuplicadoParaActualizar: () -> Unit = {},
    onContinuarBorrador: () -> Unit = {},
    onEmpezarNuevoBorrador: () -> Unit = {},
    onDescartarBorrador: () -> Unit = {},
    onGuardarBorrador: () -> Unit = {},
    onCerrarDialogoBorrador: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DuocHeader(
            title = if (uiState.isUpdatingExisting) "Actualizar Prospecto" else "Cupón de Admisión",
            subtitle = if (uiState.isExpressMode) {
                "Modo Express (Fast-Track) • Duoc UC"
            } else {
                "Captura digital por etapas • Duoc UC"
            },
            onBack = onBack,
            trailingAction = {
                TextButton(
                    onClick = onGuardarBorrador,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Guardar borrador",
                        color = DuocAmarillo,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )

        // Selector rápido Completo (6 pasos) vs Express (Fast-Track)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = !uiState.isExpressMode,
                onClick = { onToggleExpressMode(false) },
                label = { Text("Modo Completo (6 pasos)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            FilterChip(
                selected = uiState.isExpressMode,
                onClick = { onToggleExpressMode(true) },
                label = { Text("Express (Fast-Track)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DuocAmarillo,
                    selectedLabelColor = DuocAzul
                )
            )
        }

        if (!uiState.isExpressMode) {
            StepProgressBar(
                currentStep = uiState.currentStep,
                stepStatuses = uiState.stepStatuses,
                progress = uiState.progress,
                onStepSelected = onStepSelected
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.isUpdatingExisting) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Editando ficha existente (${uiState.rut}). Al guardar se marcará pendiente de sincronización.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = DuocAzul,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (!permissionBannerMessage.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = permissionBannerMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (uiState.isExpressMode) {
                ExpressCaptureForm(
                    uiState = uiState,
                    onConsentimientoChange = onConsentimientoChange,
                    onFirmaChange = onFirmaChange,
                    onScanCedulaClick = onScanCedulaClick,
                    onRutChange = onRutChange,
                    onNombresChange = onNombresChange,
                    onApellidosChange = onApellidosChange,
                    onCelularChange = onCelularChange,
                    onEmailChange = onEmailChange,
                    onSedeChange = onSedeChange,
                    onEscuelaSeleccionadaChange = onEscuelaSeleccionadaChange,
                    onCarrera1Change = onCarrera1Change,
                    onToggleAttachLocation = onToggleAttachLocation,
                    onVerFichaDuplicada = onVerFichaDuplicada,
                    onCargarDuplicadoParaActualizar = onCargarDuplicadoParaActualizar
                )
            } else {
                AnimatedContent(
                    targetState = uiState.currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "stepScreenTransition"
                ) { step ->
                    when (step) {
                        FormStep.CONSENTIMIENTO -> ConsentimientoStepScreen(
                            consentimiento = uiState.consentimiento,
                            consentimientoError = uiState.consentimientoError,
                            onConsentimientoChange = onConsentimientoChange,
                            firmaDigitalBase64 = uiState.firmaDigitalBase64,
                            firmaTimestampEpoch = uiState.firmaTimestampEpoch,
                            firmaError = uiState.firmaError,
                            onFirmaChange = onFirmaChange
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
                            onFechaNacimientoChange = onFechaNacimientoChange,
                            scannerFeedbackMessage = uiState.scannerFeedbackMessage,
                            duplicateProspecto = uiState.duplicateProspecto,
                            onScanCedulaClick = onScanCedulaClick,
                            onVerFichaDuplicada = onVerFichaDuplicada,
                            onCargarDuplicadoParaActualizar = onCargarDuplicadoParaActualizar
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
                            onEmailChange = onEmailChange,
                            comunaSuggestions = uiState.comunaResidenciaSuggestions,
                            onSelectComunaSuggestion = onSelectComunaResidencia
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
                            onJornadaChange = onJornadaChange,
                            establecimientoSuggestions = uiState.establecimientoSuggestions,
                            onSelectEstablecimientoSuggestion = onSelectEstablecimiento,
                            comunaEstablecimientoSuggestions = uiState.comunaEstablecimientoSuggestions,
                            onSelectComunaEstablecimientoSuggestion = onSelectComunaEstablecimiento
                        )

                        FormStep.SEDE_PERFIL -> SedePerfilamientoStepScreen(
                            sedeInteres = uiState.sedeInteres,
                            sedeError = uiState.sedeError,
                            nivelInteres = uiState.nivelInteres,
                            nivelInteresError = uiState.nivelInteresError,
                            perfilMotivacional = uiState.perfilMotivacional,
                            perfilMotivacionalError = uiState.perfilMotivacionalError,
                            onSedeChange = onSedeChange,
                            onNivelInteresChange = onNivelInteresChange,
                            onPerfilMotivacionalChange = onPerfilMotivacionalChange,
                            attachLocation = uiState.attachLocation,
                            isFetchingLocation = uiState.isFetchingLocation,
                            locationStatusMessage = uiState.locationStatusMessage,
                            onToggleAttachLocation = onToggleAttachLocation
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

        // Barra inferior fija de navegación / guardado
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
                if (uiState.isExpressMode) {
                    DuocOutlinedButton(
                        text = "Borrador",
                        onClick = onGuardarBorrador,
                        modifier = Modifier.weight(0.42f)
                    )
                    DuocButton(
                        text = if (uiState.isUpdatingExisting) "Actualizar Prospecto" else "Guardar Express",
                        onClick = onGuardar,
                        isLoading = uiState.isSaving,
                        modifier = Modifier.weight(0.58f)
                    )
                } else {
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
                            text = if (uiState.isUpdatingExisting) "Actualizar Prospecto" else "Guardar Prospecto",
                            onClick = onGuardar,
                            isLoading = uiState.isSaving,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    // Diálogo: Recuperación de borrador al iniciar nueva captura
    uiState.pendingDraftToResume?.let { draft ->
        AlertDialog(
            onDismissRequest = onEmpezarNuevoBorrador,
            title = {
                Text("Borrador encontrado", fontWeight = FontWeight.Bold, color = DuocAzul)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Tienes una captura pausada guardada localmente (no sincronizable):",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• ${draft.tituloResumen}",
                        fontWeight = FontWeight.Bold,
                        color = DuocAzul
                    )
                    Text(
                        text = "• ${draft.resumenSecundario} (${if (draft.modoExpress) "Express" else "Completo"})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Actualizado: ${EventoDateUtils.formatTime(draft.updatedAtEpoch)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onContinuarBorrador,
                    colors = ButtonDefaults.buttonColors(containerColor = DuocAzul)
                ) {
                    Text("Continuar borrador")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onDescartarBorrador) {
                        Text("Descartar", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = onEmpezarNuevoBorrador) {
                        Text("Empezar nuevo")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Diálogo: Confirmación de borrador guardado manualmente
    if (uiState.draftSavedDialogVisible) {
        AlertDialog(
            onDismissRequest = onCerrarDialogoBorrador,
            title = {
                Text("Borrador guardado", fontWeight = FontWeight.Bold, color = DuocAzul)
            },
            text = {
                Text(
                    "Se guardó localmente en tu dispositivo como borrador (no sincronizable). Puedes retomarlo cuando quieras desde \"Ver Capturas y Borradores\"."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCerrarDialogoBorrador()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DuocAzul)
                ) {
                    Text("Volver al Inicio")
                }
            },
            dismissButton = {
                TextButton(onClick = onCerrarDialogoBorrador) {
                    Text("Seguir editando")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Diálogo: Ficha de solo lectura para RUT duplicado
    val dupProspecto = uiState.duplicateProspecto
    if (uiState.showDuplicateDetailDialog && dupProspecto != null) {
        ProspectoDetalleDialog(
            prospecto = dupProspecto,
            onDismiss = onCerrarFichaDuplicada,
            primaryActionLabel = "Cargar para actualizar",
            onPrimaryAction = onCargarDuplicadoParaActualizar
        )
    }

    // Diálogo: Prospecto registrado con éxito
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
                    val mensajeGuardado = if (uiState.savedSyncStatus == SyncStatus.SYNCED) {
                        "Los datos fueron validados, guardados y sincronizados con el servidor."
                    } else {
                        "Los datos fueron validados y almacenados localmente de forma segura en modo Offline."
                    }
                    Text(mensajeGuardado)
                    Spacer(Modifier.height(8.dp))
                    Text("• Postulante: ${uiState.savedProspectoNombre}", fontWeight = FontWeight.SemiBold)
                    Text("• RUT validado: ${uiState.savedProspectoRut}", fontWeight = FontWeight.SemiBold)
                    if (uiState.savedWasExpress) {
                        Text(
                            "• Estado: Express / Incompleto (enriquecible luego)",
                            style = MaterialTheme.typography.bodySmall,
                            color = DuocAzul
                        )
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun ProspectoDetalleDialog(
    prospecto: Prospecto,
    onDismiss: () -> Unit,
    primaryActionLabel: String? = null,
    onPrimaryAction: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Ficha del Prospecto",
                fontWeight = FontWeight.Bold,
                color = DuocAzul
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(prospecto.nombreCompleto, fontWeight = FontWeight.Bold, color = DuocAzul)
                Text("RUT: ${prospecto.rut}", style = MaterialTheme.typography.bodySmall)
                Text("Estado: ${prospecto.estadoCompletitud.displayName} · ${prospecto.syncStatus.displayName}", style = MaterialTheme.typography.bodySmall)
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                if (prospecto.celular.isNotBlank()) Text("Celular: ${prospecto.celular}", style = MaterialTheme.typography.bodySmall)
                if (prospecto.email.isNotBlank()) Text("Email: ${prospecto.email}", style = MaterialTheme.typography.bodySmall)
                Text("Comuna: ${prospecto.comunaResidencia}", style = MaterialTheme.typography.bodySmall)
                Text("Colegio: ${prospecto.establecimientoEducacional}", style = MaterialTheme.typography.bodySmall)
                Text("Sede: ${prospecto.sedeInteres} (${prospecto.jornadaPreferencia.displayName})", style = MaterialTheme.typography.bodySmall)
                Text("Interés: ${prospecto.nivelInteres.displayName}", style = MaterialTheme.typography.bodySmall)
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("1° Carrera: ${prospecto.carreraPrioridad1}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                prospecto.carreraPrioridad2?.let {
                    Text("2° Carrera: $it", style = MaterialTheme.typography.bodySmall)
                }
                prospecto.carreraPrioridad3?.let {
                    Text("3° Carrera: $it", style = MaterialTheme.typography.bodySmall)
                }
                if (!prospecto.firmaDigitalBase64.isNullOrBlank()) {
                    Text("Firma digital adjunta", style = MaterialTheme.typography.labelSmall, color = DuocAzul)
                }
                if (prospecto.latitud != null && prospecto.longitud != null) {
                    Text(
                        "Ubicación: ${prospecto.latitud}, ${prospecto.longitud}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        },
        confirmButton = {
            if (primaryActionLabel != null && onPrimaryAction != null) {
                Button(
                    onClick = onPrimaryAction,
                    colors = ButtonDefaults.buttonColors(containerColor = DuocAzul)
                ) {
                    Text(primaryActionLabel)
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = DuocAzul)
                ) {
                    Text("Cerrar")
                }
            }
        },
        dismissButton = {
            if (primaryActionLabel != null && onPrimaryAction != null) {
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
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

@Preview(name = "StepFormScreen - Oscuro", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
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
