package com.therionlabs.captura_digital_v2

import com.therionlabs.captura_digital_v2.data.model.Genero
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.data.remote.Indicador
import com.therionlabs.captura_digital_v2.data.remote.IndicadoresApi
import com.therionlabs.captura_digital_v2.data.remote.IndicadoresResponse
import com.therionlabs.captura_digital_v2.data.repository.AuthRepository
import com.therionlabs.captura_digital_v2.data.repository.IProspectoRepository
import com.therionlabs.captura_digital_v2.ui.dashboard.DashboardViewModel
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.util.Calendar

class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authApi = FakeAuthApi()
    private val authRepository = AuthRepository(authApi)
    private val anioActual = Calendar.getInstance().get(Calendar.YEAR)

    private class FakeProspectoRepository(initial: List<Prospecto>) : IProspectoRepository {
        private val store = initial.toMutableList()
        override fun guardarProspecto(prospecto: Prospecto) = true
        override fun getAll() = store.toList()
        override fun getByCaptador(captadorId: Int) = store.filter { it.captadorId == captadorId }
        override fun getPendingSyncCount() = 0
        override suspend fun actualizarProspecto(prospecto: Prospecto): Boolean {
            val idx = store.indexOfFirst { it.id == prospecto.id }
            if (idx != -1) store[idx] = prospecto
            return idx != -1
        }
        override suspend fun eliminarProspecto(clientUuid: String): Boolean =
            store.removeAll { it.id == clientUuid }
    }

    private class FakeIndicadoresApi(private val falla: Boolean = false) : IndicadoresApi {
        override suspend fun obtener(): IndicadoresResponse {
            if (falla) throw IOException("Sin conexión")
            return IndicadoresResponse(
                uf = Indicador("UF", 39000.0),
                dolar = Indicador("Dólar", 950.0),
                utm = Indicador("UTM", 68000.0)
            )
        }
    }

    private fun prospecto(id: String, captadorId: Int, genero: Genero, edad: Int, carrera: String) = Prospecto(
        id = id,
        captadorId = captadorId,
        rut = id,
        nombres = "Nombre",
        apellidos = "Apellido",
        genero = genero,
        fechaNacimiento = "01/01/${anioActual - edad}",
        comunaResidencia = "Viña del Mar",
        celular = "+56900000000",
        email = "$id@correo.cl",
        situacionAcademica = SituacionAcademica.CUARTO_MEDIO,
        establecimientoEducacional = "Colegio",
        comunaEstablecimiento = "Viña del Mar",
        jornadaPreferencia = Jornada.DIURNA,
        sedeInteres = "Viña del Mar",
        nivelInteres = NivelInteres.ALTO,
        perfilMotivacional = PerfilMotivacional.VOCACIONAL,
        carreraPrioridad1 = carrera,
        carreraPrioridad2 = "Analista Programador"
    )

    private val prospectos = listOf(
        prospecto("1", 2, Genero.FEMENINO, 17, "Ingeniería en Informática"),
        prospecto("2", 2, Genero.MASCULINO, 20, "Ingeniería en Informática"),
        prospecto("3", 3, Genero.FEMENINO, 30, "Gastronomía"),
        prospecto("4", 3, Genero.NO_INDICA, 40, "Diseño")
    )

    private fun crearViewModel(indicadoresFallan: Boolean = false) = DashboardViewModel(
        FakeProspectoRepository(prospectos),
        authRepository,
        FakeIndicadoresApi(indicadoresFallan)
    )

    @After
    fun tearDown() {
        authRepository.logout()
    }

    @Test
    fun captador_soloVeSusProspectos() {
        val state = crearViewModel().uiState.value

        assertFalse(state.isAdmin)
        assertEquals(2, state.total)
        assertEquals(18, state.averageAge)
    }

    @Test
    fun administrador_veTodosLosProspectos() = runTest {
        authRepository.login("admin@captura.cl", "123456")

        val state = crearViewModel().uiState.value

        assertTrue(state.isAdmin)
        assertEquals(4, state.total)
    }

    @Test
    fun calculaGeneroYRangosDeEdad() = runTest {
        authRepository.login("admin@captura.cl", "123456")

        val state = crearViewModel().uiState.value

        assertEquals(2, state.byGender["Femenino"])
        assertEquals(1, state.byGender["Masculino"])
        assertEquals(1, state.byGender["No indica"])
        assertEquals(1, state.byAge["Menor de 18"])
        assertEquals(1, state.byAge["18 a 24"])
        assertEquals(1, state.byAge["25 a 34"])
        assertEquals(1, state.byAge["35 o más"])
    }

    @Test
    fun ordenaLasCarrerasDeMayorAMenor() = runTest {
        authRepository.login("admin@captura.cl", "123456")

        val state = crearViewModel().uiState.value

        assertEquals("Analista Programador", state.byInterest.keys.first())
        assertEquals(4, state.byInterest["Analista Programador"])
        assertEquals(2, state.byInterest["Ingeniería en Informática"])
    }

    @Test
    fun cargaLosIndicadoresDeLaApiExterna() {
        val state = crearViewModel().uiState.value

        assertEquals(39000.0, state.uf!!, 0.0)
        assertEquals(950.0, state.dolar!!, 0.0)
        assertEquals(68000.0, state.utm!!, 0.0)
    }

    @Test
    fun siLaApiExternaFalla_elDashboardSigueFuncionando() {
        val state = crearViewModel(indicadoresFallan = true).uiState.value

        assertNull(state.uf)
        assertEquals(2, state.total)
    }

    @Test
    fun shareText_incluyeElResumen() {
        val texto = crearViewModel().shareText()

        assertTrue(texto.contains("Encuestas: 2"))
        assertTrue(texto.contains("Género:"))
        assertTrue(texto.contains("Carreras de interés:"))
    }

    @Test
    fun rotarNivelInteresYEliminar_actualizanDashboardEnTiempoReal() = runTest {
        val vm = crearViewModel()
        val primero = vm.uiState.value.prospectos.first()
        assertEquals(NivelInteres.ALTO, primero.nivelInteres)

        vm.rotarNivelInteres(primero)
        assertEquals(NivelInteres.MEDIO, vm.uiState.value.prospectos.first().nivelInteres)

        vm.eliminarProspecto(primero.id)
        assertEquals(1, vm.uiState.value.total)
    }
}
