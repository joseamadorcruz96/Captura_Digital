package com.therionlabs.captura_digital_v2

import com.therionlabs.captura_digital_v2.data.local.CatalogoLocalidadesDataSource
import com.therionlabs.captura_digital_v2.data.local.EstablecimientoItem
import com.therionlabs.captura_digital_v2.data.local.InMemorySessionStorage
import com.therionlabs.captura_digital_v2.data.model.AppMode
import com.therionlabs.captura_digital_v2.data.model.EstadoCompletitud
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.data.model.SyncStatus
import com.therionlabs.captura_digital_v2.data.repository.BorradorRepository
import com.therionlabs.captura_digital_v2.data.repository.EventoRepository
import com.therionlabs.captura_digital_v2.data.repository.IProspectoRepository
import com.therionlabs.captura_digital_v2.data.repository.SyncResult
import com.therionlabs.captura_digital_v2.ui.formularioModal.FormStep
import com.therionlabs.captura_digital_v2.ui.formularioModal.StepFormViewModel
import com.therionlabs.captura_digital_v2.util.CedulaScanResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StepFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeProspectoRepository(
        private val syncOnlineOnSave: Boolean = false
    ) : IProspectoRepository {
        val saved = mutableListOf<Prospecto>()
        override fun guardarProspecto(prospecto: Prospecto): Boolean {
            val idx = saved.indexOfFirst { it.id == prospecto.id || it.rut == prospecto.rut }
            if (idx != -1) saved[idx] = prospecto else saved.add(prospecto)
            return true
        }

        override fun getAll(): List<Prospecto> = saved.toList()
        override fun getByCaptador(captadorId: Int): List<Prospecto> =
            saved.filter { it.captadorId == captadorId }

        override fun getPendingSyncCount(): Int = saved.count { it.syncStatus != SyncStatus.SYNCED }

        override suspend fun sincronizarNuevosPendientes(captadorId: Int, eventoId: String?): SyncResult {
            return if (syncOnlineOnSave && saved.isNotEmpty()) {
                val ids = saved.map { it.id }
                for (i in saved.indices) {
                    saved[i] = saved[i].copy(syncStatus = SyncStatus.SYNCED)
                }
                SyncResult(ids.size, ids.size, 0, "Sincronización completada exitosamente.")
            } else {
                SyncResult(saved.size, 0, saved.size, "Sin conexión", huboErrorRed = true)
            }
        }
    }

    @Test
    fun navegacionLibre_marcaAlertaCuandoSeAbandonaPasoIncompleto() {
        val viewModel = StepFormViewModel(FakeProspectoRepository())

        assertEquals(FormStep.CONSENTIMIENTO, viewModel.uiState.value.currentStep)

        // Avanzar sin aceptar consentimiento debe permitirse pero activar alerta en paso 1
        viewModel.nextStep()

        val state = viewModel.uiState.value
        assertEquals(FormStep.DATOS_PERSONALES, state.currentStep)
        val step1 = state.stepStatuses.first { it.step == FormStep.CONSENTIMIENTO }
        assertTrue(step1.hasAlert)
        assertFalse(step1.isComplete)
        assertNotNull(state.consentimientoError)
    }

    @Test
    fun guardar_bloqueaEnvioSiFaltanEtapasValidadas() {
        val repo = FakeProspectoRepository()
        val viewModel = StepFormViewModel(repo)

        // Solo completamos paso 1
        viewModel.onConsentimientoChange(true)
        viewModel.goToStep(FormStep.CARRERAS)
        viewModel.guardar(captadorId = 2, eventoId = "EVT-TEST")

        val state = viewModel.uiState.value
        assertFalse(state.saveSuccess)
        assertTrue(repo.saved.isEmpty())
        assertNotNull(state.generalErrorMessage)
    }

    @Test
    fun guardar_persisteCuandoLasSeisEtapasEstanValidadasYSincronizaSiHayRed() = runTest {
        val repo = FakeProspectoRepository(syncOnlineOnSave = true)
        val viewModel = StepFormViewModel(repo)

        // Paso 1
        viewModel.onConsentimientoChange(true)
        viewModel.nextStep()

        // Paso 2
        viewModel.onRutChange("111111111")
        viewModel.onNombresChange("Camila Andrea")
        viewModel.onApellidosChange("Valenzuela Soto")
        viewModel.nextStep()

        // Paso 3
        viewModel.onComunaResidenciaChange("Viña del Mar")
        viewModel.onEmailChange("camila@duoc.cl")
        viewModel.nextStep()

        // Paso 4
        viewModel.onEstablecimientoChange("Colegio Rubén Castro")
        viewModel.nextStep()

        // Paso 5 (sin preselecciones: abandonar vacío debe marcar alerta)
        viewModel.nextStep()
        val step5WithAlert = viewModel.uiState.value.stepStatuses.first { it.step == FormStep.SEDE_PERFIL }
        assertTrue(step5WithAlert.hasAlert)
        assertFalse(step5WithAlert.isComplete)
        assertNotNull(viewModel.uiState.value.sedeError)
        assertNotNull(viewModel.uiState.value.nivelInteresError)
        assertNotNull(viewModel.uiState.value.perfilMotivacionalError)

        // Volvemos al paso 5 y completamos sus campos
        viewModel.previousStep()
        viewModel.onSedeChange("Viña del Mar")
        viewModel.onNivelInteresChange(NivelInteres.ALTO)
        viewModel.onPerfilMotivacionalChange(PerfilMotivacional.VOCACIONAL)
        viewModel.nextStep()

        // Paso 6
        viewModel.onCarrera1Change("Ingeniería en Informática")

        viewModel.guardar(captadorId = 2, eventoId = "EVT-TEST")

        val state = viewModel.uiState.value
        assertTrue(state.saveSuccess)
        assertEquals(SyncStatus.SYNCED, state.savedSyncStatus)
        assertEquals(1, repo.saved.size)
        assertEquals("11.111.111-1", repo.saved.first().rut)
        assertEquals(1f, state.progress, 0.001f)
    }

    @Test
    fun guardar_sinEventoExplicitoUsaEventoActivoPersistidoEnEventoRepository() = runTest {
        val storage = InMemorySessionStorage(defaultAppMode = AppMode.LOCAL_DEMO)
        val eventoRepo = EventoRepository(
            eventoDao = null,
            turnoDao = null,
            api = null,
            sessionStorage = storage,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )
        eventoRepo.seleccionarEventoActivo("EVT-COL-RUBEN-2026")

        val repo = FakeProspectoRepository(syncOnlineOnSave = true)
        val viewModel = StepFormViewModel(repo, eventoRepo)

        viewModel.onConsentimientoChange(true)
        viewModel.onRutChange("111111111")
        viewModel.onNombresChange("Camila Andrea")
        viewModel.onApellidosChange("Valenzuela Soto")
        viewModel.onComunaResidenciaChange("Viña del Mar")
        viewModel.onEmailChange("camila@duoc.cl")
        viewModel.onEstablecimientoChange("Colegio Rubén Castro")
        viewModel.onSedeChange("Viña del Mar")
        viewModel.onNivelInteresChange(NivelInteres.ALTO)
        viewModel.onPerfilMotivacionalChange(PerfilMotivacional.VOCACIONAL)
        viewModel.onCarrera1Change("Ingeniería en Informática")

        viewModel.guardar(captadorId = 2)

        assertTrue(viewModel.uiState.value.saveSuccess)
        assertEquals(1, repo.saved.size)
        assertEquals("EVT-COL-RUBEN-2026", repo.saved.first().eventoId)
    }

    @Test
    fun borrador_exigeMinimoUnCampo_soportaMultiples_ySeEliminaAlGuardarProspecto() = runTest {
        val repo = FakeProspectoRepository(syncOnlineOnSave = true)
        val borradorRepo = BorradorRepository(dao = null)
        val vm = StepFormViewModel(
            repository = repo,
            borradorRepository = borradorRepo,
            autoSaveDebounceMs = 0L
        )

        // Formulario totalmente vacío (o solo consentimiento) no guarda borrador
        vm.onConsentimientoChange(true)
        vm.guardarComoBorradorExplicito(captadorId = 2)
        assertFalse(vm.uiState.value.draftSavedDialogVisible)
        assertNotNull(vm.uiState.value.generalErrorMessage)
        assertTrue(borradorRepo.observeByCaptador(2).first().isEmpty())

        // Al ingresar 1 dato (nombre) sí guarda explícitamente sin validar el resto
        vm.onNombresChange("Sofía")
        vm.guardarComoBorradorExplicito(captadorId = 2)
        assertTrue(vm.uiState.value.draftSavedDialogVisible)
        assertEquals(1, borradorRepo.observeByCaptador(2).first().size)
        assertTrue(repo.saved.isEmpty())

        // Al abrir nueva captura detecta borrador previo; si elige empezar nuevo conserva el anterior
        val vm2 = StepFormViewModel(
            repository = repo,
            borradorRepository = borradorRepo,
            autoSaveDebounceMs = 0L
        )
        vm2.inicializarCaptura(captadorId = 2)
        assertNotNull(vm2.uiState.value.pendingDraftToResume)
        vm2.empezarCapturaNuevaManteniendoBorradores()
        assertNull(vm2.uiState.value.pendingDraftToResume)

        // Retomar el borrador y completar en Modo Express debe eliminar el borrador al guardar
        val draftId = borradorRepo.observeByCaptador(2).first().first().id
        val vm3 = StepFormViewModel(
            repository = repo,
            borradorRepository = borradorRepo,
            autoSaveDebounceMs = 0L
        )
        vm3.inicializarCaptura(captadorId = 2, borradorId = draftId)
        assertEquals("Sofía", vm3.uiState.value.nombres)

        vm3.toggleExpressMode(true)
        vm3.onConsentimientoChange(true)
        vm3.onRutChange("111111111")
        vm3.onApellidosChange("Araya")
        vm3.onCelularChange("98765432")
        vm3.onSedeChange("Viña del Mar")
        vm3.onCarrera1Change("Ingeniería en Informática")
        vm3.guardar(captadorId = 2, eventoId = "EVT-TEST")

        assertTrue(vm3.uiState.value.saveSuccess)
        assertNull(borradorRepo.getById(draftId))
    }

    @Test
    fun modoExpress_validaMinimos_aplicaDefaultsSeguros_yGuardaFirmaYUbicacion() = runTest {
        val repo = FakeProspectoRepository(syncOnlineOnSave = false)
        val vm = StepFormViewModel(repository = repo, autoSaveDebounceMs = 0L)

        vm.toggleExpressMode(true)
        vm.onConsentimientoChange(true)
        vm.onFirmaChange("base64-firma-demo")
        vm.onRutChange("111111111")
        vm.onNombresChange("Lucas")
        vm.onApellidosChange("González")
        vm.onCelularChange("91234567")
        vm.onSedeChange("Viña del Mar")
        vm.onCarrera1Change("Analista Programador")
        vm.onLocationObtained(-33.0245, -71.5518)

        vm.guardar(captadorId = 2, eventoId = "EVT-EXP")

        val state = vm.uiState.value
        assertTrue(state.saveSuccess)
        assertTrue(state.savedWasExpress)
        assertEquals(1, repo.saved.size)
        val saved = repo.saved.first()
        assertEquals(EstadoCompletitud.EXPRESS, saved.estadoCompletitud)
        assertEquals("Por completar", saved.comunaResidencia)
        assertEquals("Por completar", saved.establecimientoEducacional)
        assertEquals("base64-firma-demo", saved.firmaDigitalBase64)
        assertNotNull(saved.firmaTimestampEpoch)
        assertEquals(-33.0245, saved.latitud!!, 0.0001)
        assertEquals(-71.5518, saved.longitud!!, 0.0001)
    }

    @Test
    fun cedulaEscaneada_detectaRutDuplicado_yCargaParaActualizarConservandoId() = runTest {
        val existente = Prospecto(
            id = "uuid-original-77",
            captadorId = 2,
            eventoId = "EVT-1",
            rut = "11.111.111-1",
            nombres = "Camila",
            apellidos = "Valenzuela",
            comunaResidencia = "Viña del Mar",
            celular = "+56987654321",
            email = "camila@duoc.cl",
            situacionAcademica = SituacionAcademica.CUARTO_MEDIO,
            establecimientoEducacional = "Colegio Rubén Castro",
            comunaEstablecimiento = "Viña del Mar",
            jornadaPreferencia = Jornada.DIURNA,
            sedeInteres = "Viña del Mar",
            nivelInteres = NivelInteres.MEDIO,
            perfilMotivacional = PerfilMotivacional.VOCACIONAL,
            carreraPrioridad1 = "Diseño Gráfico",
            syncStatus = SyncStatus.SYNCED
        )
        val repo = FakeProspectoRepository(syncOnlineOnSave = true).apply {
            saved.add(existente)
        }
        val vm = StepFormViewModel(repository = repo, autoSaveDebounceMs = 0L)

        vm.onCedulaScanned(
            CedulaScanResult(
                rut = "11.111.111-1",
                nombres = "Camila Andrea",
                apellidos = "Valenzuela Soto"
            )
        )

        assertNotNull(vm.uiState.value.duplicateProspecto)
        vm.cargarDuplicadoParaActualizar()

        assertNull(vm.uiState.value.duplicateProspecto)
        assertEquals("uuid-original-77", vm.uiState.value.editingProspectoId)
        assertTrue(vm.uiState.value.isUpdatingExisting)

        vm.onCarrera1Change("Ingeniería en Informática")
        vm.guardar(captadorId = 2, eventoId = "EVT-2")

        assertTrue(vm.uiState.value.saveSuccess)
        assertEquals(1, repo.saved.size)
        assertEquals("uuid-original-77", repo.saved.first().id)
        assertEquals("Ingeniería en Informática", repo.saved.first().carreraPrioridad1)
    }

    @Test
    fun autocompletado_enViewModel_filtraComunasYColegiosYAutorrellenaComunaColegio() {
        val repo = FakeProspectoRepository()
        val dataSource = CatalogoLocalidadesDataSource(
            comunasOverride = listOf("Viña del Mar", "Valparaíso", "Quilpué"),
            establecimientosOverride = listOf(
                EstablecimientoItem("Colegio Rubén Castro", "Viña del Mar", "Valparaíso"),
                EstablecimientoItem("Colegio Aconcagua", "Quilpué", "Valparaíso")
            )
        )
        val vm = StepFormViewModel(repository = repo, localidadesDataSource = dataSource)

        vm.onComunaResidenciaChange("vina")
        assertEquals(listOf("Viña del Mar"), vm.uiState.value.comunaResidenciaSuggestions)
        vm.onSelectComunaResidencia("Viña del Mar")
        assertTrue(vm.uiState.value.comunaResidenciaSuggestions.isEmpty())

        vm.onEstablecimientoChange("ruben")
        assertEquals(1, vm.uiState.value.establecimientoSuggestions.size)
        vm.onSelectEstablecimiento(vm.uiState.value.establecimientoSuggestions.first())

        assertEquals("Colegio Rubén Castro", vm.uiState.value.establecimientoEducacional)
        assertEquals("Viña del Mar", vm.uiState.value.comunaEstablecimiento)
    }
}
