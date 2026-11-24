package com.therionlabs.captura_digital_v2

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.therionlabs.captura_digital_v2.data.local.DataStoreSessionStorage
import com.therionlabs.captura_digital_v2.data.local.InMemorySessionStorage
import com.therionlabs.captura_digital_v2.data.local.LocalUserDataSource
import com.therionlabs.captura_digital_v2.data.model.AppMode
import com.therionlabs.captura_digital_v2.data.model.UserRole
import com.therionlabs.captura_digital_v2.data.remote.RetrofitClient
import com.therionlabs.captura_digital_v2.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val api = FakeAuthApi()
    private val repository = AuthRepository(api)

    @After
    fun tearDown() {
        repository.logout()
        RetrofitClient.token = null
    }

    @Test
    fun loginCorrecto_devuelveUsuarioYQuedaEnSesion() = runTest {
        val user = repository.login("admin@captura.cl", "123456")

        assertEquals("Administrador", user?.name)
        assertEquals(UserRole.ADMINISTRATIVO, user?.role)
        assertEquals(1, repository.getCurrentUser().id)
    }

    @Test
    fun loginIncorrecto_devuelveNull() = runTest {
        assertNull(repository.login("admin@captura.cl", "000000"))
    }

    @Test
    fun sinSesion_devuelveCaptadorPorDefecto() {
        assertEquals(UserRole.CAPTADOR, repository.getCurrentUser().role)
    }

    @Test
    fun logout_cierraLaSesion() = runTest {
        repository.login("admin@captura.cl", "123456")
        repository.logout()

        assertEquals(UserRole.CAPTADOR, repository.getCurrentUser().role)
        assertNull(repository.getCurrentUserOrNull())
    }

    @Test
    fun recuperarConCorreoRegistrado_devuelveTrue() = runTest {
        assertTrue(repository.recuperar("admin@captura.cl"))
    }

    @Test
    fun recuperarConCorreoNoRegistrado_devuelveFalse() = runTest {
        assertFalse(repository.recuperar("nadie@captura.cl"))
    }

    @Test
    fun localDemo_permiteLoginParaLosTresRolesSinRed() = runTest {
        api.sinConexion = true
        val localRepo = AuthRepository(
            api = api,
            localDataSource = LocalUserDataSource(),
            sessionStorage = InMemorySessionStorage(defaultAppMode = AppMode.LOCAL_DEMO)
        )

        val admin = localRepo.login(LocalUserDataSource.DEMO_ADMIN.email, LocalUserDataSource.DEMO_PASSWORD)
        assertEquals(UserRole.ADMINISTRATIVO, admin?.role)

        val captador = localRepo.login(LocalUserDataSource.DEMO_CAPTADOR.email, LocalUserDataSource.DEMO_PASSWORD)
        assertEquals(UserRole.CAPTADOR, captador?.role)

        val prospecto = localRepo.login(LocalUserDataSource.DEMO_PROSPECTO.email, LocalUserDataSource.DEMO_PASSWORD)
        assertEquals(UserRole.PROSPECTO, prospecto?.role)
        assertTrue(localRepo.recuperar(LocalUserDataSource.DEMO_PROSPECTO.email))
        assertFalse(localRepo.recuperar("desconocido@duoc.cl"))
    }

    @Test
    fun dataStoreSession_persisteSesionYAppModeEntreReiniciosYSeLimpiaEnLogout() {
        runBlocking {
        val prefsFile = tempFolder.newFile("auth_session_test.preferences_pb")
        val scope1 = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val dataStore1 = PreferenceDataStoreFactory.create(
            scope = scope1,
            produceFile = { prefsFile }
        )
        val storage1 = DataStoreSessionStorage(dataStore1, defaultAppMode = AppMode.LOCAL_DEMO, scope = scope1)
        storage1.awaitInitialLoad()

        val repo1 = AuthRepository(
            api = api,
            localDataSource = LocalUserDataSource(),
            sessionStorage = storage1,
            scope = scope1
        )
        repo1.setAppMode(AppMode.LOCAL_DEMO)
        val logged = repo1.login(LocalUserDataSource.DEMO_ADMIN.email, LocalUserDataSource.DEMO_PASSWORD)
        assertNotNull(logged)
        scope1.coroutineContext[Job]?.cancelAndJoin()

        // Simula reinicio de la app abriendo un nuevo DataStore sobre el mismo archivo
        val scope2 = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val dataStore2 = PreferenceDataStoreFactory.create(
            scope = scope2,
            produceFile = { prefsFile }
        )
        val storage2 = DataStoreSessionStorage(dataStore2, defaultAppMode = AppMode.REMOTE_GATEWAY, scope = scope2)
        storage2.awaitInitialLoad()

        val repo2 = AuthRepository(
            api = api,
            localDataSource = LocalUserDataSource(),
            sessionStorage = storage2,
            scope = scope2
        )

        assertEquals(AppMode.LOCAL_DEMO, repo2.appMode)
        assertEquals(UserRole.ADMINISTRATIVO, repo2.getCurrentUserOrNull()?.role)
        assertEquals(LocalUserDataSource.DEMO_ADMIN.email, repo2.getCurrentUser().email)

        repo2.logoutSuspend()
        assertNull(repo2.getCurrentUserOrNull())
        assertEquals(AppMode.LOCAL_DEMO, repo2.appMode)
        scope2.coroutineContext[Job]?.cancelAndJoin()
        }
    }
}
