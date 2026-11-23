package com.therionlabs.mslogin.service;

import com.therionlabs.mslogin.dto.LoginRequest;
import com.therionlabs.mslogin.dto.UsuarioRequest;
import com.therionlabs.mslogin.dto.UsuarioResponse;
import com.therionlabs.mslogin.model.Rol;
import com.therionlabs.mslogin.model.Usuario;
import com.therionlabs.mslogin.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioServiceCrudTest {

    private UsuarioRepository repository;
    private UsuarioService service;

    @BeforeEach
    void setUp() {
        repository = mock(UsuarioRepository.class);
        service = new UsuarioService(repository);
    }

    @Test
    void crearGuardaElUsuarioConSusDatosYContrasenaCifrada() {
        when(repository.existsByEmail("nuevo@captura.cl")).thenReturn(false);
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse respuesta = service.crear(
                new UsuarioRequest("Nuevo", "nuevo@captura.cl", "123456", Rol.CAPTADOR));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).save(captor.capture());
        assertEquals("Nuevo", captor.getValue().getNombre());
        assertEquals(Rol.CAPTADOR, captor.getValue().getRol());
        assertTrue(new BCryptPasswordEncoder().matches("123456", captor.getValue().getPassword()));
        assertEquals("nuevo@captura.cl", respuesta.email());
        assertTrue(respuesta.activo());
    }

    @Test
    void crearConCorreoRepetidoDevuelve409YNoGuarda() {
        when(repository.existsByEmail("admin@captura.cl")).thenReturn(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.crear(
                new UsuarioRequest("Admin", "admin@captura.cl", "123456", Rol.ADMINISTRATIVO)));

        assertEquals(409, error.getStatusCode().value());
        verify(repository, never()).save(any(Usuario.class));
    }

    @Test
    void listarConvierteTodosLosUsuariosARespuestas() {
        when(repository.findAll()).thenReturn(List.of(
                usuario(1L, "admin@captura.cl", "123456", Rol.ADMINISTRATIVO),
                usuario(2L, "captador@captura.cl", "123456", Rol.CAPTADOR)));

        List<UsuarioResponse> respuesta = service.listar();

        assertEquals(2, respuesta.size());
        assertEquals("captador@captura.cl", respuesta.get(1).email());
    }

    @Test
    void obtenerDevuelveElUsuarioExistente() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuario(1L, "admin@captura.cl", "123456", Rol.ADMINISTRATIVO)));

        assertEquals("admin@captura.cl", service.obtener(1L).email());
    }

    @Test
    void obtenerConIdInexistenteDevuelve404() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.obtener(99L));

        assertEquals(404, error.getStatusCode().value());
    }

    @Test
    void actualizarCambiaLosDatosYVuelveACifrarLaContrasena() {
        Usuario existente = usuario(1L, "captador@captura.cl", "123456", Rol.CAPTADOR);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse respuesta = service.actualizar(1L,
                new UsuarioRequest("Cambiado", "captador@captura.cl", "abcdef", Rol.ADMINISTRATIVO));

        assertEquals("Cambiado", respuesta.nombre());
        assertEquals(Rol.ADMINISTRATIVO, respuesta.rol());
        assertNotEquals("abcdef", existente.getPassword());
        assertTrue(new BCryptPasswordEncoder().matches("abcdef", existente.getPassword()));
        verify(repository, never()).existsByEmail(any(String.class));
    }

    @Test
    void actualizarConCorreoDeOtroUsuarioDevuelve409() {
        Usuario existente = usuario(1L, "captador@captura.cl", "123456", Rol.CAPTADOR);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByEmail("admin@captura.cl")).thenReturn(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.actualizar(1L,
                new UsuarioRequest("Captador", "admin@captura.cl", "123456", Rol.CAPTADOR)));

        assertEquals(409, error.getStatusCode().value());
        verify(repository, never()).save(any(Usuario.class));
    }

    @Test
    void actualizarConIdInexistenteDevuelve404() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.actualizar(99L,
                new UsuarioRequest("Nadie", "nadie@captura.cl", "123456", Rol.CAPTADOR)));

        assertEquals(404, error.getStatusCode().value());
    }

    @Test
    void eliminarBorraElUsuarioExistente() {
        Usuario existente = usuario(1L, "captador@captura.cl", "123456", Rol.CAPTADOR);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        service.eliminar(1L);

        verify(repository).delete(existente);
    }

    @Test
    void eliminarConIdInexistenteDevuelve404YNoBorra() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.eliminar(99L));

        assertEquals(404, error.getStatusCode().value());
        verify(repository, never()).delete(any(Usuario.class));
    }

    @Test
    void loginConCorreoInexistenteDevuelve401() {
        when(repository.findByEmail("nadie@captura.cl")).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.login(new LoginRequest("nadie@captura.cl", "123456")));

        assertEquals(401, error.getStatusCode().value());
    }

    @Test
    void loginConUsuarioInactivoDevuelve401() {
        Usuario inactivo = usuario(1L, "captador@captura.cl", "123456", Rol.CAPTADOR);
        inactivo.setActivo(false);
        when(repository.findByEmail("captador@captura.cl")).thenReturn(Optional.of(inactivo));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.login(new LoginRequest("captador@captura.cl", "123456")));

        assertEquals(401, error.getStatusCode().value());
    }

    @Test
    void loginConContrasenaIncorrectaDevuelve401() {
        when(repository.findByEmail("admin@captura.cl"))
                .thenReturn(Optional.of(usuario(1L, "admin@captura.cl", "123456", Rol.ADMINISTRATIVO)));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.login(new LoginRequest("admin@captura.cl", "000000")));

        assertEquals(401, error.getStatusCode().value());
    }

    @Test
    void recuperarConCorreoRegistradoNoLanzaError() {
        when(repository.existsByEmail("admin@captura.cl")).thenReturn(true);

        service.recuperar("admin@captura.cl");

        verify(repository).existsByEmail("admin@captura.cl");
    }

    @Test
    void recuperarConCorreoNoRegistradoDevuelve404() {
        when(repository.existsByEmail("nadie@captura.cl")).thenReturn(false);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.recuperar("nadie@captura.cl"));

        assertEquals(404, error.getStatusCode().value());
    }

    private Usuario usuario(Long id, String email, String password, Rol rol) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombre("Usuario " + id);
        u.setEmail(email);
        u.setPassword(new BCryptPasswordEncoder().encode(password));
        u.setRol(rol);
        return u;
    }
}
