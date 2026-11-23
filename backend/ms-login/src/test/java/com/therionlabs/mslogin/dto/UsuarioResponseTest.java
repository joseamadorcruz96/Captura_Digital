package com.therionlabs.mslogin.dto;

import com.therionlabs.mslogin.model.Rol;
import com.therionlabs.mslogin.model.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioResponseTest {

    @Test
    void fromCopiaLosDatosDelUsuarioSinLaContrasena() {
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setNombre("Captador");
        usuario.setEmail("captador@captura.cl");
        usuario.setPassword("hash-secreto");
        usuario.setRol(Rol.CAPTADOR);
        usuario.setActivo(false);

        UsuarioResponse respuesta = UsuarioResponse.from(usuario);

        assertEquals(7L, respuesta.id());
        assertEquals("Captador", respuesta.nombre());
        assertEquals("captador@captura.cl", respuesta.email());
        assertEquals(Rol.CAPTADOR, respuesta.rol());
        assertFalse(respuesta.activo());
    }

    @Test
    void fromRespetaElEstadoActivoPorDefecto() {
        Usuario usuario = new Usuario();
        usuario.setRol(Rol.ADMINISTRATIVO);

        assertTrue(UsuarioResponse.from(usuario).activo());
    }

    @Test
    void loginResponseGuardaTokenYUsuario() {
        UsuarioResponse usuario = new UsuarioResponse(1L, "Administrador", "admin@captura.cl", Rol.ADMINISTRATIVO, true);

        LoginResponse respuesta = new LoginResponse("token-123", usuario);

        assertEquals("token-123", respuesta.token());
        assertEquals(usuario, respuesta.usuario());
    }
}
