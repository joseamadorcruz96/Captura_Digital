package com.therionlabs.mslogin.controller;

import com.therionlabs.mslogin.dto.LoginRequest;
import com.therionlabs.mslogin.dto.LoginResponse;
import com.therionlabs.mslogin.dto.UsuarioResponse;
import com.therionlabs.mslogin.security.JwtService;
import com.therionlabs.mslogin.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService service;
    private final JwtService jwtService;

    public AuthController(UsuarioService service, JwtService jwtService) {
        this.service = service;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        UsuarioResponse usuario = service.login(request);
        return new LoginResponse(jwtService.generar(usuario), usuario);
    }

    @PostMapping("/recuperar")
    public Map<String, String> recuperar(@RequestBody Map<String, String> body) {
        service.recuperar(body.get("email"));
        return Map.of("mensaje", "Enviamos las instrucciones a " + body.get("email"));
    }
}