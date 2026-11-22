package com.therionlabs.msformulario.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Filtro stateless que reconstruye la identidad y rol del usuario autenticado por ms-login
 * a partir de las cabeceras X-User-Id y X-User-Rol (y X-App-Key opcional en producción).
 */
@Slf4j
@Component
public class HeaderContextAuthFilter extends OncePerRequestFilter {

    private static final Set<String> VALID_ROLES = Set.of("CAPTADOR", "ADMINISTRATIVO", "PROSPECTO");

    private final String expectedAppKey;

    public HeaderContextAuthFilter(@Value("${security.app-key:}") String expectedAppKey) {
        this.expectedAppKey = expectedAppKey != null ? expectedAppKey.trim() : "";
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!expectedAppKey.isEmpty()) {
            String providedKey = request.getHeader("X-App-Key");
            if (!expectedAppKey.equals(providedKey)) {
                log.warn("Petición rechazada por X-App-Key inválida en {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }
        }

        String userId = request.getHeader("X-User-Id");
        String userRol = request.getHeader("X-User-Rol");

        if (userId != null && !userId.isBlank() && userRol != null && !userRol.isBlank()) {
            String normalizedRole = userRol.trim().toUpperCase();
            if (VALID_ROLES.contains(normalizedRole)) {
                var auth = new UsernamePasswordAuthenticationToken(
                        userId.trim(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + normalizedRole))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                log.warn("Rol desconocido recibido en X-User-Rol: {}", userRol);
            }
        }

        filterChain.doFilter(request, response);
    }
}
