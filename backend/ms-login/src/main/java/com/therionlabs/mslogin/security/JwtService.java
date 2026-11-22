package com.therionlabs.mslogin.security;

import com.therionlabs.mslogin.dto.UsuarioResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final long EXPIRACION_MS = 8 * 60 * 60 * 1000;

    private final SecretKey key;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generar(UsuarioResponse usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.email())
                .claim("rol", usuario.rol().name())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + EXPIRACION_MS))
                .signWith(key)
                .compact();
    }

    public Claims validar(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}