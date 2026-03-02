package com.novacore.config;

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
 * OpenAPI 3 (Swagger) configuration.
 * UI: /swagger-ui.html or /swagger-ui/index.html
 * JSON: /api-docs (OpenAPI 3.0 spec; "v3" = spec version, not app API version)
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT access token. Login via POST /api/auth/login, then paste the accessToken here.")));
    }

    private Info apiInfo() {
        return new Info()
                .title("NovaCore API")
                .description("REST API for NovaCore Backend. Most endpoints require JWT authentication. " +
                        "Use **Authorize** to set the Bearer token after login.")
                .version("1.0.0")
                .contact(new Contact()
                        .name("NovaCore")
                        .email("support@novacore.com"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://novacore.com/license"));
    }

    private List<Server> servers() {
        Server local = new Server()
                .url(contextPath.isEmpty() ? "/" : contextPath)
                .description("Local");
        return List.of(local);
    }
}
