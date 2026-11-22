package com.therionlabs.mslogin.dto;

public record LoginResponse(
        String token,
        UsuarioResponse usuario
) {}