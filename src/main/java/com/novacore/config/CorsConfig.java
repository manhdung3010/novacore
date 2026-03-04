package com.novacore.config;

import com.novacore.config.properties.CorsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Central CORS configuration.
 *
 * Notes:
 * - Spring Security will pick up this {@link CorsConfigurationSource} when {@code http.cors()} is enabled.
 * - Settings are sourced from {@code cors.*} in {@code application.yml}.
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    private final CorsProperties corsProperties;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        // cors.setAllowedOrigins(corsProperties.getAllowedOrigins());
        // cors.setAllowedMethods(corsProperties.getAllowedMethods());
        // cors.setAllowedHeaders(corsProperties.getAllowedHeaders());
        // cors.setAllowCredentials(corsProperties.isAllowCredentials());

        cors.addAllowedOriginPattern("*");
        cors.addAllowedHeader("*");
        cors.addAllowedMethod("*");
        cors.setAllowCredentials(true);
        cors.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Keep in sync with WebConfig.addCorsMappings (API-only CORS)
        source.registerCorsConfiguration("/api/**", cors);
        return source;
    }
}

