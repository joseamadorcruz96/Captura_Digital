package com.therionlabs.msformulario.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI msFormularioOpenAPI() {
        final String schemeName = "bearerAuth";
        return new OpenAPI()
                .addServersItem(new Server().url("/").description("API Gateway"))
                .info(new Info()
                        .title("MS Formulario - Captura Digital API")
                        .version("v1")
                        .description("Gestión de prospectos, eventos y sincronización Offline-First."))
                .components(new Components()
                        .addSecuritySchemes(schemeName, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(schemeName));
    }
}
