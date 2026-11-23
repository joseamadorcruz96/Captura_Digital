package com.therionlabs.mslogin.security;

import com.therionlabs.mslogin.dto.UsuarioResponse;
import com.therionlabs.mslogin.model.Rol;
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

class JwtServiceTest {

    private static final String SECRET = "clave-de-prueba-para-ms-login-con-mas-de-32-bytes";

    private JwtService service;

    @BeforeEach
    void setUp() {
        service = new JwtService(SECRET);
    }

    @Test
    void generarCreaUnTokenConCorreoYRol() {
        UsuarioResponse usuario = new UsuarioResponse(1L, "Administrador", "admin@captura.cl", Rol.ADMINISTRATIVO, true);

        Claims claims = service.validar(service.generar(usuario));

        assertEquals("admin@captura.cl", claims.getSubject());
        assertEquals("ADMINISTRATIVO", claims.get("rol", String.class));
    }

    @Test
    void elTokenDuraOchoHoras() {
        UsuarioResponse usuario = new UsuarioResponse(2L, "Captador", "captador@captura.cl", Rol.CAPTADOR, true);

        Claims claims = service.validar(service.generar(usuario));

        long duracion = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertEquals(8L * 60 * 60 * 1000, duracion);
    }

    @Test
    void validarRechazaTokenExpirado() {
        Date pasado = new Date(System.currentTimeMillis() - 120_000L);
        String expirado = Jwts.builder()
                .subject("admin@captura.cl")
                .claim("rol", "ADMINISTRATIVO")
                .issuedAt(new Date(pasado.getTime() - 60_000L))
                .expiration(pasado)
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThrows(ExpiredJwtException.class, () -> service.validar(expirado));
    }

    @Test
    void validarRechazaTokenFirmadoConOtraClave() {
        String ajeno = Jwts.builder()
                .subject("atacante@evil.cl")
                .claim("rol", "ADMINISTRATIVO")
                .expiration(new Date(System.currentTimeMillis() + 60_000L))
                .signWith(Keys.hmacShaKeyFor("otra-clave-secreta-distinta-min-32-bytes!".getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThrows(JwtException.class, () -> service.validar(ajeno));
    }

    @Test
    void validarRechazaTokenMalFormado() {
        assertThrows(JwtException.class, () -> service.validar("no.es.un.jwt"));
    }

    @Test
    void rechazaUnaClaveDemasiadoCorta() {
        assertThrows(RuntimeException.class, () -> new JwtService("corta"));
    }
}
