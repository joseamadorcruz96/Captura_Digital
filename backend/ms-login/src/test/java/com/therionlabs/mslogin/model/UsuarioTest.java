package com.therionlabs.mslogin.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioTest {

    @Test
    void unUsuarioNuevoEstaActivoYSinIdentificador() {
        Usuario usuario = new Usuario();

        assertTrue(usuario.isActivo());
        assertNull(usuario.getId());
    }

    @Test
    void guardaYEntregaTodosSusCampos() {
        Usuario usuario = new Usuario();
        usuario.setId(3L);
        usuario.setNombre("Administrador");
        usuario.setEmail("admin@captura.cl");
        usuario.setPassword("hash");
        usuario.setRol(Rol.ADMINISTRATIVO);
        usuario.setActivo(false);

        assertEquals(3L, usuario.getId());
        assertEquals("Administrador", usuario.getNombre());
        assertEquals("admin@captura.cl", usuario.getEmail());
        assertEquals("hash", usuario.getPassword());
        assertEquals(Rol.ADMINISTRATIVO, usuario.getRol());
        assertFalse(usuario.isActivo());
    }
}
