package com.therionlabs.msformulario.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtValidationService jwtValidationService;

    @Mock
    private FilterChain filterChain;

    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new JwtAuthFilter(jwtValidationService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noAutenticaCuandoFaltaHeaderOSinPrefijoBearer() throws Exception {
        MockHttpServletRequest sinHeader = new MockHttpServletRequest();
        filter.doFilterInternal(sinHeader, new MockHttpServletResponse(), filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        MockHttpServletRequest sinBearer = new MockHttpServletRequest();
        sinBearer.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        filter.doFilterInternal(sinBearer, new MockHttpServletResponse(), filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verifyNoInteractions(jwtValidationService);
    }

    @Test
    void cargaAutenticacionYRolCuandoTokenEsValido() throws Exception {
        Claims claims = Jwts.claims().subject("admin@duoc.cl").add("rol", "administrativo").build();
        when(jwtValidationService.parseClaims("token-ok")).thenReturn(claims);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer   token-ok  ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("admin@duoc.cl", auth.getPrincipal());
        assertEquals("ROLE_ADMINISTRATIVO", auth.getAuthorities().iterator().next().getAuthority());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void limpiaContextoCuandoSubjectORolEsNuloOInvalido() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("previo", null));

        Claims sinSubject = Jwts.claims().add("rol", "CAPTADOR").build();
        when(jwtValidationService.parseClaims("t1")).thenReturn(sinSubject);
        MockHttpServletRequest r1 = new MockHttpServletRequest();
        r1.addHeader("Authorization", "Bearer t1");
        filter.doFilterInternal(r1, new MockHttpServletResponse(), filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        Claims sinRol = Jwts.claims().subject("user@duoc.cl").build();
        when(jwtValidationService.parseClaims("t2")).thenReturn(sinRol);
        MockHttpServletRequest r2 = new MockHttpServletRequest();
        r2.addHeader("Authorization", "Bearer t2");
        filter.doFilterInternal(r2, new MockHttpServletResponse(), filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        Claims rolDesconocido = Jwts.claims().subject("user@duoc.cl").add("rol", "INVITADO").build();
        when(jwtValidationService.parseClaims("t3")).thenReturn(rolDesconocido);
        MockHttpServletRequest r3 = new MockHttpServletRequest();
        r3.addHeader("Authorization", "Bearer t3");
        filter.doFilterInternal(r3, new MockHttpServletResponse(), filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void limpiaContextoCuandoParseClaimsLanzaJwtOIllegalArgumentException() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("previo", null));
        when(jwtValidationService.parseClaims("malo")).thenThrow(new JwtException("Firma inválida"));

        MockHttpServletRequest r1 = new MockHttpServletRequest();
        r1.setRequestURI("/api/v1/prospectos");
        r1.addHeader("Authorization", "Bearer malo");
        filter.doFilterInternal(r1, new MockHttpServletResponse(), filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("previo", null));
        when(jwtValidationService.parseClaims("vacio")).thenThrow(new IllegalArgumentException("JWT vacío"));

        MockHttpServletRequest r2 = new MockHttpServletRequest();
        r2.setRequestURI("/api/v1/prospectos");
        r2.addHeader("Authorization", "Bearer vacio");
        filter.doFilterInternal(r2, new MockHttpServletResponse(), filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
