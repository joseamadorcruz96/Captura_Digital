package com.therionlabs.captura_digital_v2.ui.formularioModal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.therionlabs.captura_digital_v2.data.local.CatalogoLocalidadesDataSource
import com.therionlabs.captura_digital_v2.data.local.DemoSeedGenerator
import com.therionlabs.captura_digital_v2.data.local.EstablecimientoItem
import com.therionlabs.captura_digital_v2.data.model.CatalogoDuoc
import com.therionlabs.captura_digital_v2.data.model.EstadoCompletitud
import com.therionlabs.captura_digital_v2.data.model.FormularioBorrador
import com.therionlabs.captura_digital_v2.data.model.Genero
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.data.model.SyncStatus
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.repository.BorradorRepository
import com.therionlabs.captura_digital_v2.data.repository.IBorradorRepository
import com.therionlabs.captura_digital_v2.data.repository.IEventoRepository
import com.therionlabs.captura_digital_v2.data.repository.IProspectoRepository
import com.therionlabs.captura_digital_v2.data.repository.ProspectoRepository
import com.therionlabs.captura_digital_v2.util.CedulaScanResult
import com.therionlabs.captura_digital_v2.util.FormularioValidator
import com.therionlabs.captura_digital_v2.util.RutUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel para StepForm 2.0 (Modo Completo de 6 pasos y Modo Express Fast-Track).
 * Gestiona autoguardado y guardado explícito de borradores locales (mínimo 1 campo, no sincronizables),
 * firma digital, escáner de cédula, detección inteligente de RUT duplicado, autocompletado de
 * comunas/establecimientos y geolocalización opcional.
 */
class StepFormViewModel(
    private val repository: IProspectoRepository,
    private val eventoRepository: IEventoRepository? = null,
    private val borradorRepository: IBorradorRepository = BorradorRepository(),
    private val localidadesDataSource: CatalogoLocalidadesDataSource = CatalogoLocalidadesDataSource(),
    private val exigirFirma: Boolean = false,
    private val autoSaveDebounceMs: Long = 350L,
    private val nowProvider: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    constructor() : this(ProspectoRepository(), null)

    private val _uiState = MutableStateFlow(StepFormUiState())
    val uiState: StateFlow<StepFormUiState> = _uiState.asStateFlow()

    private var activeCaptadorId: Int? = null
    private var activeEventoIdArg: String? = null
    private var initializedKey: String? = null
    private var autoSaveJob: Job? = null
    private var duplicateCheckJob: Job? = null
    private var lastCheckedDuplicateRut: String? = null

    // --- Inicialización y ciclo de borradores ---

    /**
     * Inicializa la sesión de captura según el punto de entrada:
     * - Con [borradorId]: carga directamente ese borrador para retomarlo.
     * - Con [prospectoId]: carga el prospecto existente para completar/actualizar (conservando su ID).
     * - Sin ambos (Nueva Captura): consulta si existe un borrador reciente y ofrece Continuar / Nuevo / Descartar.
     */
    fun inicializarCaptura(
        captadorId: Int,
        eventoId: String? = null,
        borradorId: String? = null,
        prospectoId: String? = null
    ) {
        activeCaptadorId = captadorId
        activeEventoIdArg = eventoId

        val key = "$captadorId:${borradorId.orEmpty()}:${prospectoId.orEmpty()}"
        if (initializedKey == key) return
        initializedKey = key

        viewModelScope.launch {
            when {
                !borradorId.isNullOrBlank() -> {
                    borradorRepository.getById(borradorId)?.let { borrador ->
                        restaurarDesdeBorrador(borrador)
                    }
                }
                !prospectoId.isNullOrBlank() -> {
                    repository.buscarPorId(prospectoId)?.let { prospecto ->
                        cargarProspectoExistente(prospecto)
                    }
                }
                else -> {
                    val latest = borradorRepository.getLatestByCaptador(captadorId)
                    if (latest != null && latest.hasMinimumDraftData()) {
                        _uiState.update { it.copy(pendingDraftToResume = latest) }
                    }
                }
            }
        }
    }

    /**
     * Permite habilitar autoguardado directo desde tests unitarios sin depender de [inicializarCaptura].
     */
    fun activarAutoguardado(captadorId: Int, eventoId: String? = null) {
        activeCaptadorId = captadorId
        activeEventoIdArg = eventoId
    }

    fun continuarBorradorPendiente() {
        val draft = _uiState.value.pendingDraftToResume ?: return
        restaurarDesdeBorrador(draft)
    }

    fun empezarCapturaNuevaManteniendoBorradores() {
        _uiState.update { state ->
            StepFormUiState(
                escuelaSeleccionadaId = state.escuelaSeleccionadaId,
                draftId = UUID.randomUUID().toString(),
                pendingDraftToResume = null
            )
        }
        lastCheckedDuplicateRut = null
    }

    fun descartarBorradorPendiente() {
        val draft = _uiState.value.pendingDraftToResume ?: return
        _uiState.update { it.copy(pendingDraftToResume = null) }
        viewModelScope.launch {
            borradorRepository.eliminarBorrador(draft.id)
        }
    }

    /**
     * Guarda explícitamente el formulario actual como borrador local (no sincronizable).
     * Exige mínimo 1 campo con datos reales (ej. nombre o RUT).
     */
    fun guardarComoBorradorExplicito(captadorId: Int, eventoId: String? = null) {
        activeCaptadorId = captadorId
        activeEventoIdArg = eventoId
        autoSaveJob?.cancel()

        val state = _uiState.value
        val draft = state.toFormularioBorrador(
            captadorId = captadorId,
            eventoId = eventoId ?: activeEventoIdArg,
            nowEpoch = nowProvider()
        )
        val validation = FormularioValidator.validarBorradorMinimo(draft)
        if (!validation.isValid) {
            _uiState.update {
                it.copy(generalErrorMessage = validation.errorOrNull())
            }
            return
        }

        viewModelScope.launch {
            borradorRepository.guardarBorrador(draft)
            _uiState.update {
                it.copy(
                    draftId = draft.id,
                    generalErrorMessage = null,
                    lastDraftSavedEpoch = draft.updatedAtEpoch,
                    draftSavedDialogVisible = true
                )
            }
        }
    }

    fun cerrarDialogoBorradorGuardado() {
        _uiState.update { it.copy(draftSavedDialogVisible = false) }
    }

    // --- Modo Express vs Completo ---

    fun toggleExpressMode(express: Boolean) {
        _uiState.update { state ->
            state.copy(
                isExpressMode = express,
                generalErrorMessage = null
            )
        }
        scheduleAutoSave()
    }

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
        scheduleAutoSave()
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

    fun onFirmaChange(base64: String?) {
        val normalized = base64?.takeIf { it.isNotBlank() }
        updateStateAndRefreshStep(FormStep.CONSENTIMIENTO) { state ->
            state.copy(
                firmaDigitalBase64 = normalized,
                firmaTimestampEpoch = if (normalized != null) nowProvider() else null,
                firmaError = if (exigirFirma && normalized == null) {
                    FormularioValidator.validarFirmaDigital(null).errorOrNull()
                } else {
                    null
                }
            )
        }
    }

    fun onRutChange(value: String) {
        var resolvedFormattedRut = ""
        updateStateAndRefreshStep(FormStep.DATOS_PERSONALES) { state ->
            if (value.endsWith("-")) {
                val raw = RutUtils.cleanRut(value)
                resolvedFormattedRut = raw
                return@updateStateAndRefreshStep state.copy(
                    rut = raw,
                    rutError = null,
                    duplicateProspecto = null,
                    scannerFeedbackMessage = null
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
            resolvedFormattedRut = formatted

            val error = when {
                limited.length == 9 && !RutUtils.isValidRut(limited) ->
                    "RUT inválido (revise dígito verificador)"
                limited.length == 8 && limited.endsWith("K") && !RutUtils.isValidRut(limited) ->
                    "RUT inválido (revise dígito verificador)"
                else -> null
            }

            state.copy(
                rut = formatted,
                rutError = error,
                duplicateProspecto = if (RutUtils.isValidRut(formatted)) state.duplicateProspecto else null,
                scannerFeedbackMessage = null
            )
        }

        verificarRutDuplicado(resolvedFormattedRut)
    }

    fun onCedulaScanned(scan: CedulaScanResult) {
        updateStateAndRefreshStep(FormStep.DATOS_PERSONALES) { state ->
            state.copy(
                rut = scan.rut,
                rutError = null,
                nombres = scan.nombres?.takeIf { it.isNotBlank() } ?: state.nombres,
                nombresError = if (!scan.nombres.isNullOrBlank()) null else state.nombresError,
                apellidos = scan.apellidos?.takeIf { it.isNotBlank() } ?: state.apellidos,
                apellidosError = if (!scan.apellidos.isNullOrBlank()) null else state.apellidosError,
                fechaNacimiento = scan.fechaNacimiento?.takeIf { it.isNotBlank() } ?: state.fechaNacimiento,
                scannerFeedbackMessage = "Cédula escaneada (${scan.sourceFormat}): ${scan.rut}"
            )
        }
        lastCheckedDuplicateRut = null
        verificarRutDuplicado(scan.rut)
    }

    fun onScannerFeedbackClear() {
        _uiState.update { it.copy(scannerFeedbackMessage = null) }
    }

    fun abrirFichaDuplicada() {
        if (_uiState.value.duplicateProspecto != null) {
            _uiState.update { it.copy(showDuplicateDetailDialog = true) }
        }
    }

    fun cerrarFichaDuplicada() {
        _uiState.update { it.copy(showDuplicateDetailDialog = false) }
    }

    fun cargarDuplicadoParaActualizar() {
        val dup = _uiState.value.duplicateProspecto ?: return
        cargarProspectoExistente(dup)
    }

    private fun cargarProspectoExistente(prospecto: Prospecto) {
        val celularSinPrefijo = prospecto.celular.removePrefix("+569").filter { it.isDigit() }
        val escuelaId = CatalogoDuoc.carreras
            .firstOrNull { it.nombre.equals(prospecto.carreraPrioridad1, ignoreCase = true) }
            ?.escuelaId
            ?: _uiState.value.escuelaSeleccionadaId

        val cleanComunaResidencia = prospecto.comunaResidencia.takeUnless { it == "Por completar" }.orEmpty()
        val cleanEstablecimiento = prospecto.establecimientoEducacional.takeUnless { it == "Por completar" }.orEmpty()
        val cleanComunaEstab = prospecto.comunaEstablecimiento.takeUnless { it == "Por completar" }.orEmpty()

        _uiState.update { state ->
            val updated = state.copy(
                editingProspectoId = prospecto.id,
                duplicateProspecto = null,
                showDuplicateDetailDialog = false,
                consentimiento = prospecto.consentimientoDatos,
                consentimientoError = null,
                firmaDigitalBase64 = prospecto.firmaDigitalBase64,
                firmaTimestampEpoch = prospecto.firmaTimestampEpoch,
                firmaError = null,
                rut = prospecto.rut,
                rutError = null,
                nombres = prospecto.nombres,
                nombresError = null,
                apellidos = prospecto.apellidos,
                apellidosError = null,
                genero = prospecto.genero,
                fechaNacimiento = prospecto.fechaNacimiento.orEmpty(),
                comunaResidencia = cleanComunaResidencia,
                comunaResidenciaError = null,
                comunaResidenciaSuggestions = emptyList(),
                celular = celularSinPrefijo,
                celularError = null,
                email = prospecto.email,
                emailError = null,
                situacionAcademica = prospecto.situacionAcademica,
                situacionAcademicaOtro = prospecto.situacionAcademicaOtro.orEmpty(),
                establecimientoEducacional = cleanEstablecimiento,
                establecimientoError = null,
                establecimientoSuggestions = emptyList(),
                comunaEstablecimiento = cleanComunaEstab,
                comunaEstablecimientoSuggestions = emptyList(),
                jornada = prospecto.jornadaPreferencia,
                sedeInteres = prospecto.sedeInteres,
                sedeError = null,
                nivelInteres = prospecto.nivelInteres,
                nivelInteresError = null,
                perfilMotivacional = prospecto.perfilMotivacional,
                perfilMotivacionalError = null,
                escuelaSeleccionadaId = escuelaId,
                carreraPrioridad1 = prospecto.carreraPrioridad1,
                carrera1Error = null,
                carreraPrioridad2 = prospecto.carreraPrioridad2.orEmpty(),
                carreraPrioridad3 = prospecto.carreraPrioridad3.orEmpty(),
                latitud = prospecto.latitud,
                longitud = prospecto.longitud,
                attachLocation = prospecto.latitud != null && prospecto.longitud != null,
                generalErrorMessage = null
            )
            updated.copy(stepStatuses = recalculateStatuses(updated, setVisited = updated.currentStep))
        }
        lastCheckedDuplicateRut = prospecto.rut
        scheduleAutoSave()
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

    // --- Autocompletado inteligente en Contacto y Antecedentes ---

    fun onComunaResidenciaChange(value: String) {
        val suggestions = localidadesDataSource.buscarComunas(value)
            .filterNot { it.equals(value.trim(), ignoreCase = true) }
        updateStateAndRefreshStep(FormStep.CONTACTO) { state ->
            val error = if (state.comunaResidenciaError != null) {
                FormularioValidator.validarComunaResidencia(value).errorOrNull()
            } else null
            state.copy(
                comunaResidencia = value,
                comunaResidenciaError = error,
                comunaResidenciaSuggestions = suggestions
            )
        }
    }

    fun onSelectComunaResidencia(comuna: String) {
        updateStateAndRefreshStep(FormStep.CONTACTO) { state ->
            state.copy(
                comunaResidencia = comuna,
                comunaResidenciaError = FormularioValidator.validarComunaResidencia(comuna).errorOrNull(),
                comunaResidenciaSuggestions = emptyList()
            )
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
        val currentState = _uiState.value
        val comunaPreferida = currentState.comunaEstablecimiento.ifBlank { currentState.comunaResidencia }
        val suggestions = localidadesDataSource.buscarEstablecimientos(
            query = value,
            comunaFiltro = comunaPreferida
        ).filterNot { it.nombre.equals(value.trim(), ignoreCase = true) }

        updateStateAndRefreshStep(FormStep.ANTECEDENTES) { state ->
            val error = if (state.establecimientoError != null) {
                FormularioValidator.validarEstablecimiento(value).errorOrNull()
            } else null
            state.copy(
                establecimientoEducacional = value,
                establecimientoError = error,
                establecimientoSuggestions = suggestions
            )
        }
    }

    fun onSelectEstablecimiento(item: EstablecimientoItem) {
        updateStateAndRefreshStep(FormStep.ANTECEDENTES) { state ->
            val nextComuna = state.comunaEstablecimiento.ifBlank { item.comuna }
            state.copy(
                establecimientoEducacional = item.nombre,
                establecimientoError = FormularioValidator.validarEstablecimiento(item.nombre).errorOrNull(),
                establecimientoSuggestions = emptyList(),
                comunaEstablecimiento = nextComuna,
                comunaEstablecimientoSuggestions = emptyList()
            )
        }
    }

    fun onComunaEstablecimientoChange(value: String) {
        val suggestions = localidadesDataSource.buscarComunas(value)
            .filterNot { it.equals(value.trim(), ignoreCase = true) }
        updateStateAndRefreshStep(FormStep.ANTECEDENTES) { state ->
            state.copy(
                comunaEstablecimiento = value,
                comunaEstablecimientoSuggestions = suggestions
            )
        }
    }

    fun onSelectComunaEstablecimiento(comuna: String) {
        updateStateAndRefreshStep(FormStep.ANTECEDENTES) { state ->
            state.copy(
                comunaEstablecimiento = comuna,
                comunaEstablecimientoSuggestions = emptyList()
            )
        }
    }

    fun onJornadaChange(value: Jornada) {
        updateStateAndRefreshStep(FormStep.ANTECEDENTES) { state ->
            state.copy(jornada = value)
        }
    }

    fun onSedeChange(value: String) {
        updateStateAndRefreshStep(FormStep.SEDE_PERFIL) { state ->
            state.copy(
                sedeInteres = value,
                sedeError = FormularioValidator.validarSede(value).errorOrNull()
            )
        }
    }

    fun onNivelInteresChange(value: NivelInteres) {
        updateStateAndRefreshStep(FormStep.SEDE_PERFIL) { state ->
            state.copy(
                nivelInteres = value,
                nivelInteresError = null
            )
        }
    }

    fun onPerfilMotivacionalChange(value: PerfilMotivacional) {
        updateStateAndRefreshStep(FormStep.SEDE_PERFIL) { state ->
            state.copy(
                perfilMotivacional = value,
                perfilMotivacionalError = null
            )
        }
    }

    // --- Geolocalización opcional ---

    fun onAttachLocationStarted() {
        _uiState.update {
            it.copy(
                attachLocation = true,
                isFetchingLocation = true,
                locationStatusMessage = "Obteniendo ubicación aproximada..."
            )
        }
    }

    fun onLocationCleared() {
        _uiState.update {
            it.copy(
                attachLocation = false,
                isFetchingLocation = false,
                latitud = null,
                longitud = null,
                locationStatusMessage = null
            )
        }
        scheduleAutoSave()
    }

    fun onLocationObtained(latitud: Double, longitud: Double) {
        _uiState.update {
            it.copy(
                attachLocation = true,
                isFetchingLocation = false,
                latitud = latitud,
                longitud = longitud,
                locationStatusMessage = "Ubicación adjunta (${latitud}, ${longitud})"
            )
        }
        scheduleAutoSave()
    }

    fun onLocationUnavailable(message: String) {
        _uiState.update {
            it.copy(
                attachLocation = false,
                isFetchingLocation = false,
                latitud = null,
                longitud = null,
                locationStatusMessage = message
            )
        }
    }

    fun onEscuelaSeleccionadaChange(escuelaId: String) {
        _uiState.update { it.copy(escuelaSeleccionadaId = escuelaId) }
        scheduleAutoSave()
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

    // --- Guardado con validación diferenciada: Modo Express vs Completo ---

    fun guardar(captadorId: Int, eventoId: String? = null) {
        autoSaveJob?.cancel()
        val current = _uiState.value

        if (current.isExpressMode) {
            guardarModoExpress(current, captadorId, eventoId)
            return
        }

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

        val sede = validatedState.sedeInteres ?: return
        val nivel = validatedState.nivelInteres ?: return
        val perfil = validatedState.perfilMotivacional ?: return

        persistirProspecto(
            validatedState = validatedState.copy(stepStatuses = updatedStatuses),
            captadorId = captadorId,
            eventoId = eventoId,
            sede = sede,
            nivel = nivel,
            perfil = perfil,
            estadoCompletitud = EstadoCompletitud.COMPLETO
        )
    }

    private fun guardarModoExpress(
        current: StepFormUiState,
        captadorId: Int,
        eventoId: String?
    ) {
        val contacto = FormularioValidator.validarContacto(current.email, current.celular)
        val validatedState = current.copy(
            consentimientoError = FormularioValidator.validarConsentimiento(current.consentimiento).errorOrNull(),
            firmaError = if (exigirFirma) FormularioValidator.validarFirmaDigital(current.firmaDigitalBase64).errorOrNull() else null,
            rutError = FormularioValidator.validarRut(current.rut).errorOrNull(),
            nombresError = FormularioValidator.validarNombres(current.nombres).errorOrNull(),
            apellidosError = FormularioValidator.validarApellidos(current.apellidos).errorOrNull(),
            emailError = contacto.emailResultado.errorOrNull(),
            celularError = contacto.celularResultado.errorOrNull(),
            sedeError = FormularioValidator.validarSede(current.sedeInteres).errorOrNull(),
            carrera1Error = FormularioValidator.validarCarreraPrioridad1(current.carreraPrioridad1).errorOrNull()
        )

        val expressErrors = FormularioValidator.validarExpress(
            consentimiento = validatedState.consentimiento,
            firmaBase64 = validatedState.firmaDigitalBase64,
            exigirFirma = exigirFirma,
            rut = validatedState.rut,
            nombres = validatedState.nombres,
            apellidos = validatedState.apellidos,
            email = validatedState.email,
            celular = validatedState.celular,
            sedeInteres = validatedState.sedeInteres,
            carreraPrioridad1 = validatedState.carreraPrioridad1
        )

        if (expressErrors.isNotEmpty()) {
            _uiState.value = validatedState.copy(
                generalErrorMessage = "Modo Express requiere: ${expressErrors.first()}"
            )
            return
        }

        val sede = validatedState.sedeInteres ?: return
        val nivel = validatedState.nivelInteres ?: NivelInteres.MEDIO
        val perfil = validatedState.perfilMotivacional ?: PerfilMotivacional.VOCACIONAL

        persistirProspecto(
            validatedState = validatedState,
            captadorId = captadorId,
            eventoId = eventoId,
            sede = sede,
            nivel = nivel,
            perfil = perfil,
            estadoCompletitud = EstadoCompletitud.EXPRESS
        )
    }

    private fun persistirProspecto(
        validatedState: StepFormUiState,
        captadorId: Int,
        eventoId: String?,
        sede: String,
        nivel: NivelInteres,
        perfil: PerfilMotivacional,
        estadoCompletitud: EstadoCompletitud
    ) {
        _uiState.value = validatedState.copy(
            generalErrorMessage = null,
            isSaving = true
        )

        viewModelScope.launch {
            val resolvedEventoId = eventoId?.takeIf { it.isNotBlank() }
                ?: activeEventoIdArg?.takeIf { it.isNotBlank() }
                ?: eventoRepository?.getEventoActivoId()
                ?: DemoSeedGenerator.DEFAULT_EVENT_ID

            val resolvedComunaResidencia = validatedState.comunaResidencia.trim().ifBlank {
                if (estadoCompletitud == EstadoCompletitud.EXPRESS) "Por completar" else ""
            }
            val resolvedEstablecimiento = validatedState.establecimientoEducacional.trim().ifBlank {
                if (estadoCompletitud == EstadoCompletitud.EXPRESS) "Por completar" else ""
            }
            val resolvedComunaEstab = validatedState.comunaEstablecimiento.trim().ifBlank {
                resolvedComunaResidencia.ifBlank { "Por completar" }
            }

            val prospecto = Prospecto(
                id = validatedState.editingProspectoId ?: UUID.randomUUID().toString(),
                captadorId = captadorId,
                eventoId = resolvedEventoId,
                fechaCaptura = nowProvider(),
                consentimientoDatos = true,
                rut = RutUtils.formatRut(validatedState.rut),
                nombres = validatedState.nombres.trim(),
                apellidos = validatedState.apellidos.trim(),
                genero = validatedState.genero,
                fechaNacimiento = validatedState.fechaNacimiento.ifBlank { null },
                comunaResidencia = resolvedComunaResidencia,
                celular = if (validatedState.celular.isNotBlank()) "+569${validatedState.celular}" else "",
                email = validatedState.email.trim(),
                situacionAcademica = validatedState.situacionAcademica,
                situacionAcademicaOtro = validatedState.situacionAcademicaOtro.ifBlank { null },
                establecimientoEducacional = resolvedEstablecimiento,
                comunaEstablecimiento = resolvedComunaEstab,
                jornadaPreferencia = validatedState.jornada,
                sedeInteres = sede,
                nivelInteres = nivel,
                perfilMotivacional = perfil,
                carreraPrioridad1 = validatedState.carreraPrioridad1,
                carreraPrioridad2 = validatedState.carreraPrioridad2.ifBlank { null },
                carreraPrioridad3 = validatedState.carreraPrioridad3.ifBlank { null },
                syncStatus = SyncStatus.PENDING_SYNC,
                estadoCompletitud = estadoCompletitud,
                firmaDigitalBase64 = validatedState.firmaDigitalBase64,
                firmaTimestampEpoch = validatedState.firmaTimestampEpoch,
                latitud = validatedState.latitud,
                longitud = validatedState.longitud
            )

            val finalStatus = if (validatedState.editingProspectoId != null) {
                repository.actualizarProspecto(prospecto)
                repository.buscarPorId(prospecto.id)?.syncStatus ?: SyncStatus.PENDING_SYNC
            } else {
                repository.guardarProspectoSuspend(prospecto)
                val syncResult = repository.sincronizarNuevosPendientes(
                    captadorId = captadorId,
                    eventoId = resolvedEventoId
                )
                if (syncResult.exitosos > 0 && syncResult.fallidos == 0) {
                    SyncStatus.SYNCED
                } else {
                    SyncStatus.PENDING_SYNC
                }
            }

            validatedState.draftId?.let { draftToDelete ->
                borradorRepository.eliminarBorrador(draftToDelete)
            }

            _uiState.update { state ->
                state.copy(
                    isSaving = false,
                    saveSuccess = true,
                    savedProspectoNombre = prospecto.nombreCompleto,
                    savedProspectoRut = prospecto.rut,
                    savedSyncStatus = finalStatus,
                    savedWasExpress = estadoCompletitud == EstadoCompletitud.EXPRESS,
                    draftId = null
                )
            }
        }
    }

    fun resetFormulario() {
        autoSaveJob?.cancel()
        lastCheckedDuplicateRut = null
        _uiState.update { state ->
            StepFormUiState(
                escuelaSeleccionadaId = state.escuelaSeleccionadaId,
                isExpressMode = state.isExpressMode
            )
        }
    }

    // --- Helpers internos ---

    private fun restaurarDesdeBorrador(borrador: FormularioBorrador) {
        val restoredStep = runCatching { FormStep.valueOf(borrador.currentStep) }
            .getOrDefault(FormStep.CONSENTIMIENTO)

        _uiState.update {
            val base = StepFormUiState(
                isExpressMode = borrador.modoExpress,
                consentimiento = borrador.consentimiento,
                firmaDigitalBase64 = borrador.firmaDigitalBase64,
                firmaTimestampEpoch = borrador.firmaTimestampEpoch,
                rut = borrador.rut,
                nombres = borrador.nombres,
                apellidos = borrador.apellidos,
                genero = borrador.genero,
                fechaNacimiento = borrador.fechaNacimiento,
                comunaResidencia = borrador.comunaResidencia,
                celular = borrador.celular,
                email = borrador.email,
                situacionAcademica = borrador.situacionAcademica,
                situacionAcademicaOtro = borrador.situacionAcademicaOtro,
                establecimientoEducacional = borrador.establecimientoEducacional,
                comunaEstablecimiento = borrador.comunaEstablecimiento,
                jornada = borrador.jornada,
                sedeInteres = borrador.sedeInteres,
                nivelInteres = borrador.nivelInteres,
                perfilMotivacional = borrador.perfilMotivacional,
                escuelaSeleccionadaId = borrador.escuelaSeleccionadaId,
                carreraPrioridad1 = borrador.carreraPrioridad1,
                carreraPrioridad2 = borrador.carreraPrioridad2,
                carreraPrioridad3 = borrador.carreraPrioridad3,
                latitud = borrador.latitud,
                longitud = borrador.longitud,
                attachLocation = borrador.latitud != null && borrador.longitud != null,
                locationStatusMessage = if (borrador.latitud != null && borrador.longitud != null) {
                    "Ubicación adjunta (${borrador.latitud}, ${borrador.longitud})"
                } else null,
                editingProspectoId = borrador.editingProspectoId,
                currentStep = restoredStep,
                draftId = borrador.id,
                pendingDraftToResume = null,
                lastDraftSavedEpoch = borrador.updatedAtEpoch
            )
            base.copy(stepStatuses = recalculateStatuses(base, setVisited = restoredStep))
        }
        if (RutUtils.isValidRut(borrador.rut)) {
            verificarRutDuplicado(borrador.rut)
        }
    }

    private fun verificarRutDuplicado(rut: String) {
        if (!RutUtils.isValidRut(rut)) return
        val formatted = RutUtils.formatRut(rut)
        if (formatted == lastCheckedDuplicateRut) return
        lastCheckedDuplicateRut = formatted

        duplicateCheckJob?.cancel()
        duplicateCheckJob = viewModelScope.launch {
            val found = repository.buscarPorRut(formatted)
            val editingId = _uiState.value.editingProspectoId
            val duplicate = if (found != null && found.id != editingId) found else null
            _uiState.update { it.copy(duplicateProspecto = duplicate) }
        }
    }

    private fun scheduleAutoSave() {
        val captadorId = activeCaptadorId ?: return
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            if (autoSaveDebounceMs > 0L) {
                delay(autoSaveDebounceMs)
            }
            val current = _uiState.value
            if (current.saveSuccess || current.isSaving || current.pendingDraftToResume != null) return@launch
            val draft = current.toFormularioBorrador(
                captadorId = captadorId,
                eventoId = activeEventoIdArg,
                nowEpoch = nowProvider()
            )
            if (draft.hasMinimumDraftData()) {
                borradorRepository.guardarBorrador(draft)
                _uiState.update {
                    if (it.draftId == null) {
                        it.copy(draftId = draft.id, lastDraftSavedEpoch = draft.updatedAtEpoch)
                    } else {
                        it.copy(lastDraftSavedEpoch = draft.updatedAtEpoch)
                    }
                }
            }
        }
    }

    private fun StepFormUiState.toFormularioBorrador(
        captadorId: Int,
        eventoId: String?,
        nowEpoch: Long
    ): FormularioBorrador {
        return FormularioBorrador(
            id = draftId ?: UUID.randomUUID().toString(),
            captadorId = captadorId,
            eventoId = eventoId,
            modoExpress = isExpressMode,
            currentStep = currentStep.name,
            consentimiento = consentimiento,
            firmaDigitalBase64 = firmaDigitalBase64,
            firmaTimestampEpoch = firmaTimestampEpoch,
            rut = rut,
            nombres = nombres,
            apellidos = apellidos,
            genero = genero,
            fechaNacimiento = fechaNacimiento,
            comunaResidencia = comunaResidencia,
            celular = celular,
            email = email,
            situacionAcademica = situacionAcademica,
            situacionAcademicaOtro = situacionAcademicaOtro,
            establecimientoEducacional = establecimientoEducacional,
            comunaEstablecimiento = comunaEstablecimiento,
            jornada = jornada,
            sedeInteres = sedeInteres,
            nivelInteres = nivelInteres,
            perfilMotivacional = perfilMotivacional,
            escuelaSeleccionadaId = escuelaSeleccionadaId,
            carreraPrioridad1 = carreraPrioridad1,
            carreraPrioridad2 = carreraPrioridad2,
            carreraPrioridad3 = carreraPrioridad3,
            latitud = latitud,
            longitud = longitud,
            editingProspectoId = editingProspectoId,
            updatedAtEpoch = nowEpoch
        )
    }

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
        scheduleAutoSave()
    }

    private fun recalculateStatuses(
        state: StepFormUiState,
        setVisited: FormStep
    ): List<StepStatus> {
        return state.stepStatuses.map { status ->
            val valid = isStepValid(state, status.step)
            val visited = status.isVisited || status.step == setVisited || valid
            status.copy(
                isVisited = visited,
                isComplete = valid,
                hasAlert = if (valid) false else status.hasAlert
            )
        }
    }

    private fun isStepValid(state: StepFormUiState, step: FormStep): Boolean {
        return when (step) {
            FormStep.CONSENTIMIENTO ->
                FormularioValidator.validarConsentimientoConFirma(
                    aceptado = state.consentimiento,
                    firmaBase64 = state.firmaDigitalBase64,
                    exigirFirma = exigirFirma
                ).isValid

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
                FormularioValidator.validarSede(state.sedeInteres).isValid &&
                    FormularioValidator.validarNivelInteres(state.nivelInteres).isValid &&
                    FormularioValidator.validarPerfilMotivacional(state.perfilMotivacional).isValid

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
                    .errorOrNull(),
                firmaError = if (exigirFirma) {
                    FormularioValidator.validarFirmaDigital(state.firmaDigitalBase64).errorOrNull()
                } else {
                    null
                }
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

            FormStep.SEDE_PERFIL -> state.copy(
                sedeError = FormularioValidator.validarSede(state.sedeInteres).errorOrNull(),
                nivelInteresError = FormularioValidator
                    .validarNivelInteres(state.nivelInteres)
                    .errorOrNull(),
                perfilMotivacionalError = FormularioValidator
                    .validarPerfilMotivacional(state.perfilMotivacional)
                    .errorOrNull()
            )

            FormStep.CARRERAS -> state.copy(
                carrera1Error = FormularioValidator
                    .validarCarreraPrioridad1(state.carreraPrioridad1)
                    .errorOrNull()
            )
        }
    }
}
