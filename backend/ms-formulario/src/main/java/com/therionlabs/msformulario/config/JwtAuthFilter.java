package com.therionlabs.msformulario.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Filtro stateless que valida Authorization: Bearer <JWT> emitido por ms-login
 * y carga el rol del usuario en SecurityContextHolder.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Set<String> VALID_ROLES = Set.of("CAPTADOR", "ADMINISTRATIVO", "PROSPECTO");

    private final JwtValidationService jwtValidationService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            try {
                Claims claims = jwtValidationService.parseClaims(token);
                String rawRole = claims.get("rol", String.class);
                String subject = claims.getSubject();

                if (subject != null && rawRole != null && VALID_ROLES.contains(rawRole.toUpperCase())) {
                    var authority = new SimpleGrantedAuthority("ROLE_" + rawRole.toUpperCase());
                    var authentication = new UsernamePasswordAuthenticationToken(subject, null, List.of(authority));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    SecurityContextHolder.clearContext();
                }
            } catch (JwtException | IllegalArgumentException ex) {
                log.debug("Token JWT rechazado en {}: {}", request.getRequestURI(), ex.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
