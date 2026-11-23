package com.therionlabs.apigateway.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Registra cada solicitud HTTP entrante al Gateway junto a su código de estado y latencia.
 */
@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startMs = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long elapsedMs = System.currentTimeMillis() - startMs;
            log.info("[GATEWAY] {} {} -> {} ({} ms)",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    elapsedMs);
        }
    }
}
