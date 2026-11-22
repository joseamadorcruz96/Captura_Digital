package com.therionlabs.msformulario.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI msFormularioOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS Formulario - Captura Digital API")
                        .version("v1")
                        .description("Gestión de prospectos, eventos y sincronización Offline-First."))
                .components(new Components()
                        .addSecuritySchemes("X-User-Id", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-User-Id"))
                        .addSecuritySchemes("X-User-Rol", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-User-Rol")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("X-User-Id")
                        .addList("X-User-Rol"));
    }
}
