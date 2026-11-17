package com.therionlabs.captura_digital_v2

import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.repository.IProspectoRepository
import com.therionlabs.captura_digital_v2.ui.formularioModal.FormStep
import com.therionlabs.captura_digital_v2.ui.formularioModal.StepFormViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StepFormViewModelTest {

    private class FakeProspectoRepository : IProspectoRepository {
        val saved = mutableListOf<Prospecto>()
        override fun guardarProspecto(prospecto: Prospecto): Boolean {
            saved.add(prospecto)
            return true
        }
        override fun getAll(): List<Prospecto> = saved.toList()
        override fun getByCaptador(captadorId: Int): List<Prospecto> =
            saved.filter { it.captadorId == captadorId }
        override fun getPendingSyncCount(): Int = saved.size
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
    fun guardar_persisteCuandoLasSeisEtapasEstanValidadas() {
        val repo = FakeProspectoRepository()
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
        assertEquals(1, repo.saved.size)
        assertEquals("11.111.111-1", repo.saved.first().rut)
        assertEquals(1f, state.progress, 0.001f)
    }
}
