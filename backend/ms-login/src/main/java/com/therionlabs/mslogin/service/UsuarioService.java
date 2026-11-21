package com.therionlabs.mslogin.service;

import com.therionlabs.mslogin.dto.LoginRequest;
import com.therionlabs.mslogin.dto.UsuarioRequest;
import com.therionlabs.mslogin.dto.UsuarioResponse;
import com.therionlabs.mslogin.model.Usuario;
import com.therionlabs.mslogin.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public UsuarioResponse crear(UsuarioRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());
        usuario.setPassword(encoder.encode(request.password()));
        usuario.setRol(request.rol());

        return UsuarioResponse.from(repository.save(usuario));
    }

    public List<UsuarioResponse> listar() {
        return repository.findAll().stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.from(buscar(id));
    }

    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        Usuario usuario = buscar(id);

        if (!usuario.getEmail().equals(request.email()) && repository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }

        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());
        usuario.setPassword(encoder.encode(request.password()));
        usuario.setRol(request.rol());

        return UsuarioResponse.from(repository.save(usuario));
    }

    public void eliminar(Long id) {
        repository.delete(buscar(id));
    }

    public UsuarioResponse login(LoginRequest request) {
        Usuario usuario = repository.findByEmail(request.email())
                .filter(Usuario::isActivo)
                .filter(u -> encoder.matches(request.password(), u.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));

        return UsuarioResponse.from(usuario);
    }

    public void recuperar(String email) {
        if (!repository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Este correo no está registrado");
        }
    }

    private Usuario buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }
}