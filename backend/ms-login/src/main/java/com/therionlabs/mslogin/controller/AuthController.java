package com.therionlabs.mslogin.controller;

import com.therionlabs.mslogin.dto.LoginRequest;
import com.therionlabs.mslogin.dto.UsuarioResponse;
import com.therionlabs.mslogin.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService service;

    public AuthController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public UsuarioResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }

    @PostMapping("/recuperar")
    public Map<String, String> recuperar(@RequestBody Map<String, String> body) {
        service.recuperar(body.get("email"));
        return Map.of("mensaje", "Enviamos las instrucciones a " + body.get("email"));
    }
}