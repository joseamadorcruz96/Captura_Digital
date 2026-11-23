package com.therionlabs.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Arrays;
import java.util.List;

/**
 * Configura el enrutamiento HTTP hacia los microservicios internos y la política CORS del Gateway.
 */
@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouterFunction<ServerResponse> gatewayRoutes(
            @Value("${services.ms-login-url}") String msLoginUrl,
            @Value("${services.ms-formulario-url}") String msFormularioUrl
    ) {
        RouterFunction<ServerResponse> docsRoute = GatewayRouterFunctions.route("ms-formulario-docs")
                .route(RequestPredicates.path("/openapi/formulario"), HandlerFunctions.http())
                .before(BeforeFilterFunctions.uri(msFormularioUrl))
                .filter(FilterFunctions.setPath("/v3/api-docs"))
                .build();

        return proxyRoute("ms-login-auth", "/api/auth/**", msLoginUrl)
                .and(proxyRoute("ms-login-usuarios", "/api/usuarios/**", msLoginUrl))
                .and(proxyRoute("ms-formulario-prospectos", "/api/v1/prospectos/**", msFormularioUrl))
                .and(proxyRoute("ms-formulario-eventos", "/api/v1/eventos/**", msFormularioUrl))
                .and(proxyRoute("ms-formulario-swagger-html", "/swagger-ui.html", msFormularioUrl))
                .and(proxyRoute("ms-formulario-swagger-ui", "/swagger-ui/**", msFormularioUrl))
                .and(proxyRoute("ms-formulario-api-docs", "/v3/api-docs/**", msFormularioUrl))
                .and(docsRoute);
    }

    private RouterFunction<ServerResponse> proxyRoute(String routeId, String pathPattern, String targetUri) {
        return GatewayRouterFunctions.route(routeId)
                .route(RequestPredicates.path(pathPattern), HandlerFunctions.http())
                .before(BeforeFilterFunctions.uri(targetUri))
                .build();
    }

    /**
     * Registra un CorsFilter con máxima precedencia para cubrir rutas funcionales del Gateway y preflight OPTIONS.
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter(
            @Value("${cors.allowed-origins}") String allowedOriginsRaw
    ) {
        List<String> origins = Arrays.stream(allowedOriginsRaw.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Idempotency-Key",
                "X-User-Id",
                "X-User-Rol",
                "X-Requested-With"
        ));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(new CorsFilter(source));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
