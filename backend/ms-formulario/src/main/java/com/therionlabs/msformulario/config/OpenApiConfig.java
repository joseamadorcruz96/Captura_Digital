package com.therionlabs.msformulario.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
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
                        .description("Gestión de prospectos, eventos y sincronización Offline-First."));
    }
}
