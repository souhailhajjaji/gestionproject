package com.gestionprojet.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) configuration class.
 * Configures API documentation with security schemes, server URLs, and metadata.
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.version:1.0.0-SNAPSHOT}")
    private String appVersion;

    /**
     * Creates and configures the OpenAPI documentation bean.
     * Sets up JWT bearer authentication security scheme and server URLs.
     *
     * @return configured OpenAPI instance
     */
    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
            .info(new Info()
                .title("Gestion Projet API")
                .description("API REST pour la gestion de projets, tâches et utilisateurs")
                .version(appVersion)
                .contact(new Contact()
                    .name("Gestion Projet Team")
                    .email("contact@gestionprojet.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8082/api")
                    .description("Local Development Server"),
                new Server()
                    .url("http://localhost:8080/api")
                    .description("Docker Compose Server")))
            .addSecurityItem(new SecurityRequirement()
                .addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT token obtenu via Keycloak")));
    }
}
