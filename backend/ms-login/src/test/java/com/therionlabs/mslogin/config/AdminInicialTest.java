package com.therionlabs.mslogin.config;

import com.therionlabs.mslogin.dto.UsuarioRequest;
import com.therionlabs.mslogin.model.Rol;
import com.therionlabs.mslogin.repository.UsuarioRepository;
import com.therionlabs.mslogin.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminInicialTest {

    private UsuarioRepository repository;
    private UsuarioService service;
    private AdminInicial adminInicial;

    @BeforeEach
    void setUp() {
        repository = mock(UsuarioRepository.class);
        service = mock(UsuarioService.class);
        adminInicial = new AdminInicial(repository, service, "admin@captura.cl", "123456");
    }

    @Test
    void creaAdministradorCuandoNoHayUsuarios() {
        when(repository.count()).thenReturn(0L);

        adminInicial.run();

        ArgumentCaptor<UsuarioRequest> captor = ArgumentCaptor.forClass(UsuarioRequest.class);
        verify(service).crear(captor.capture());
        assertEquals("Administrador", captor.getValue().nombre());
        assertEquals("admin@captura.cl", captor.getValue().email());
        assertEquals("123456", captor.getValue().password());
        assertEquals(Rol.ADMINISTRATIVO, captor.getValue().rol());
    }

    @Test
    void noCreaNadaCuandoYaExistenUsuarios() {
        when(repository.count()).thenReturn(3L);

        adminInicial.run();

        verify(service, never()).crear(any(UsuarioRequest.class));
    }
}
