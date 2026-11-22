package com.therionlabs.msformulario.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtValidationServiceTest {

    private static final String SECRET = "captura-digital-dev-secret-key-2026-min-32-bytes";
    private JwtValidationService service;

    @BeforeEach
    void setUp() {
        service = new JwtValidationService(SECRET);
    }

    @Test
    void parseClaimsRetornaClaimsCuandoTokenEsValido() {
        Date now = new Date();
        String token = Jwts.builder()
                .subject("captador@duoc.cl")
                .claim("rol", "CAPTADOR")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 60_000L))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        Claims claims = service.parseClaims(token);

        assertEquals("captador@duoc.cl", claims.getSubject());
        assertEquals("CAPTADOR", claims.get("rol", String.class));
    }

    @Test
    void parseClaimsRechazaTokenExpiradoFirmaInvalidaYMalformado() {
        Date past = new Date(System.currentTimeMillis() - 120_000L);
        String expired = Jwts.builder()
                .subject("captador@duoc.cl")
                .claim("rol", "CAPTADOR")
                .issuedAt(new Date(past.getTime() - 60_000L))
                .expiration(past)
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThrows(ExpiredJwtException.class, () -> service.parseClaims(expired));

        String wrongSecret = Jwts.builder()
                .subject("captador@duoc.cl")
                .claim("rol", "CAPTADOR")
                .expiration(new Date(System.currentTimeMillis() + 60_000L))
                .signWith(Keys.hmacShaKeyFor("otra-clave-secreta-distinta-min-32-bytes!".getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThrows(JwtException.class, () -> service.parseClaims(wrongSecret));
        assertThrows(JwtException.class, () -> service.parseClaims("no.es.un.jwt"));
    }
}
