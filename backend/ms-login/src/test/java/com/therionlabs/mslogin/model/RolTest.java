package com.therionlabs.mslogin.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RolTest {

    @Test
    void defineLosTresRolesDelSistema() {
        assertArrayEquals(
                new Rol[]{Rol.CAPTADOR, Rol.PROSPECTO, Rol.ADMINISTRATIVO},
                Rol.values());
    }

    @Test
    void valueOfConvierteTextoEnRol() {
        assertEquals(Rol.ADMINISTRATIVO, Rol.valueOf("ADMINISTRATIVO"));
        assertThrows(IllegalArgumentException.class, () -> Rol.valueOf("INVITADO"));
    }
}
