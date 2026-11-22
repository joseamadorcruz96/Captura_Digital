package com.therionlabs.mslogin.config;

import com.therionlabs.mslogin.dto.UsuarioRequest;
import com.therionlabs.mslogin.model.Rol;
import com.therionlabs.mslogin.repository.UsuarioRepository;
import com.therionlabs.mslogin.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInicial implements CommandLineRunner {

    private final UsuarioRepository repository;
    private final UsuarioService service;
    private final String email;
    private final String password;

    public AdminInicial(
            UsuarioRepository repository,
            UsuarioService service,
            @Value("${ADMIN_EMAIL:admin@captura.cl}") String email,
            @Value("${ADMIN_PASSWORD:123456}") String password
    ) {
        this.repository = repository;
        this.service = service;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            service.crear(new UsuarioRequest("Administrador", email, password, Rol.ADMINISTRATIVO));
        }
    }
}