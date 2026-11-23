package com.therionlabs.apigateway;

import com.therionlabs.apigateway.config.RequestLoggingFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ApiGatewayApplicationTests {

    @Autowired
    private RouterFunction<ServerResponse> gatewayRoutes;

    @Autowired
    private FilterRegistrationBean<CorsFilter> corsFilter;

    @Autowired
    private RequestLoggingFilter requestLoggingFilter;

    @Test
    void contextLoadsAndRoutesMatchExpectedPaths() {
        assertNotNull(gatewayRoutes);
        assertNotNull(corsFilter);
        assertNotNull(requestLoggingFilter);

        List<String> expectedPaths = List.of(
                "/api/auth/login",
                "/api/usuarios/1",
                "/api/v1/prospectos/sync",
                "/api/v1/eventos",
                "/openapi/formulario",
                "/swagger-ui.html",
                "/swagger-ui/index.html",
                "/v3/api-docs",
                "/v3/api-docs/swagger-config"
        );

        for (String path : expectedPaths) {
            MockHttpServletRequest servletRequest = new MockHttpServletRequest("GET", path);
            ServerRequest serverRequest = ServerRequest.create(servletRequest, List.of());
            assertTrue(gatewayRoutes.route(serverRequest).isPresent(), "Ruta no mapeada: " + path);
        }
    }
}
