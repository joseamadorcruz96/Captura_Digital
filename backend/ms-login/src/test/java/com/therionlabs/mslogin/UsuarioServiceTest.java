package com.therionlabs.mslogin;

import com.therionlabs.mslogin.dto.LoginRequest;
import com.therionlabs.mslogin.dto.UsuarioRequest;
import com.therionlabs.mslogin.dto.UsuarioResponse;
import com.therionlabs.mslogin.model.Rol;
import com.therionlabs.mslogin.model.Usuario;
import com.therionlabs.mslogin.repository.UsuarioRepository;
import com.therionlabs.mslogin.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    private UsuarioRepository repository;
    private UsuarioService service;

    @BeforeEach
    void setUp() {
        repository = mock(UsuarioRepository.class);
        service = new UsuarioService(repository);
    }

    @Test
    void crearUsuarioCifraLaContrasena() {
        when(repository.existsByEmail("nuevo@captura.cl")).thenReturn(false);
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UsuarioResponse respuesta = service.crear(
                new UsuarioRequest("Nuevo", "nuevo@captura.cl", "123456", Rol.CAPTADOR));

        assertEquals(1L, respuesta.id());
        assertEquals("nuevo@captura.cl", respuesta.email());
        verify(repository).save(argThat(u -> !u.getPassword().equals("123456")));
    }

    @Test
    void crearConCorreoRepetidoLanzaError() {
        when(repository.existsByEmail("admin@captura.cl")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> service.crear(
                new UsuarioRequest("Admin", "admin@captura.cl", "123456", Rol.ADMINISTRATIVO)));
    }

    @Test
    void loginCorrectoDevuelveUsuario() {
        Usuario usuario = usuario("admin@captura.cl", "123456");
        when(repository.findByEmail("admin@captura.cl")).thenReturn(Optional.of(usuario));

        UsuarioResponse respuesta = service.login(new LoginRequest("admin@captura.cl", "123456"));

        assertEquals("admin@captura.cl", respuesta.email());
    }

    @Test
    void loginConContrasenaIncorrectaLanzaError() {
        Usuario usuario = usuario("admin@captura.cl", "123456");
        when(repository.findByEmail("admin@captura.cl")).thenReturn(Optional.of(usuario));

        assertThrows(ResponseStatusException.class,
                () -> service.login(new LoginRequest("admin@captura.cl", "000000")));
    }

    @Test
    void recuperarConCorreoNoRegistradoLanzaError() {
        when(repository.existsByEmail("nadie@captura.cl")).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> service.recuperar("nadie@captura.cl"));
    }

    private Usuario usuario(String email, String password) {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setNombre("Administrador");
        u.setEmail(email);
        u.setPassword(new BCryptPasswordEncoder().encode(password));
        u.setRol(Rol.ADMINISTRATIVO);
        return u;
    }
}