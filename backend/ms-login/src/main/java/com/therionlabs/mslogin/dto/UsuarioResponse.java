package com.therionlabs.mslogin.dto;

import com.therionlabs.mslogin.model.Rol;
import com.therionlabs.mslogin.model.Usuario;

public record UsuarioResponse(
        Long id,
        String nombre,
        String email,
        Rol rol,
        boolean activo
) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.isActivo()
        );
    }
}