package com.therionlabs.mslogin.repository;

import com.therionlabs.mslogin.model.Rol;
import com.therionlabs.mslogin.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void guardarAsignaIdentificadorYDejaElUsuarioActivo() {
        Usuario guardado = repository.save(usuario("captador@captura.cl", Rol.CAPTADOR));

        assertNotNull(guardado.getId());
        assertTrue(guardado.isActivo());
        assertEquals(Rol.CAPTADOR, repository.findById(guardado.getId()).orElseThrow().getRol());
    }

    @Test
    void findByEmailDevuelveElUsuarioExistente() {
        repository.save(usuario("admin@captura.cl", Rol.ADMINISTRATIVO));

        Optional<Usuario> encontrado = repository.findByEmail("admin@captura.cl");

        assertTrue(encontrado.isPresent());
        assertEquals(Rol.ADMINISTRATIVO, encontrado.get().getRol());
    }

    @Test
    void findByEmailDevuelveVacioSiNoExiste() {
        assertTrue(repository.findByEmail("nadie@captura.cl").isEmpty());
    }

    @Test
    void existsByEmailIndicaSiElCorreoEstaRegistrado() {
        repository.save(usuario("admin@captura.cl", Rol.ADMINISTRATIVO));

        assertTrue(repository.existsByEmail("admin@captura.cl"));
        assertFalse(repository.existsByEmail("nadie@captura.cl"));
    }

    @Test
    void noPermiteDosUsuariosConElMismoCorreo() {
        repository.saveAndFlush(usuario("repetido@captura.cl", Rol.CAPTADOR));

        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(usuario("repetido@captura.cl", Rol.PROSPECTO)));
    }

    @Test
    void eliminarQuitaAlUsuario() {
        Usuario guardado = repository.save(usuario("captador@captura.cl", Rol.CAPTADOR));

        repository.delete(guardado);

        assertFalse(repository.existsByEmail("captador@captura.cl"));
        assertEquals(0, repository.count());
    }

    private Usuario usuario(String email, Rol rol) {
        Usuario u = new Usuario();
        u.setNombre("Usuario de prueba");
        u.setEmail(email);
        u.setPassword("hash");
        u.setRol(rol);
        return u;
    }
}
