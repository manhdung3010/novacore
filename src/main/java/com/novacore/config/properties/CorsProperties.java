package com.novacore.config.properties;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Configuration properties for CORS settings.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    @NotEmpty(message = "CORS allowed origins must not be empty")
    private List<String> allowedOrigins = List.of("http://localhost:3000", "http://localhost:8080");

    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");

    private List<String> allowedHeaders = List.of("*");

    private boolean allowCredentials = true;

    private long maxAge = 3600;
}







