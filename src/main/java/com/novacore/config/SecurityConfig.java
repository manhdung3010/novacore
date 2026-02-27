package com.novacore.config;

import com.novacore.auth.security.JwtAuthenticationFilter;
import com.novacore.config.properties.AuthProperties;
import com.novacore.config.properties.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;

/**
 * Security configuration for authentication and authorization.
 * 
 * Design principles:
 * - Configures JWT-based authentication
 * - Public endpoints: /api/auth/login, /api/auth/refresh, /health
 * - Protected endpoints: /api/** (requires authentication)
 * - Stateless session management (JWT tokens)
 * - CSRF disabled (not needed for stateless JWT)
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties({JwtProperties.class, AuthProperties.class})
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Creates a BCryptPasswordEncoder bean for password encoding.
     * Uses default strength of 10 rounds.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the security filter chain.
     * 
     * Public endpoints:
     * - /api/auth/login
     * - /api/auth/refresh
     * - /health
     * 
     * Protected endpoints:
     * - /api/** (requires valid JWT token)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless JWT authentication
                .csrf(AbstractHttpConfigurer::disable)
                
                // Disable form login and HTTP basic (not needed for JWT)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // Enable CORS (uses CorsConfigurationSource bean, if present)
                .cors(Customizer.withDefaults())
                
                // Stateless session management (no session cookies)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Configure authorization rules
                // IMPORTANT: Order matters! More specific patterns should come first
                .authorizeHttpRequests(auth -> auth
                        // Allow CORS preflight requests through security filter chain
                        .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()

                        // Public auth endpoints - use specific paths to ensure they're matched first
                        .requestMatchers("/api/auth/register", 
                                        "/api/auth/login", 
                                        "/api/auth/refresh", 
                                        "/api/auth/logout").permitAll()
                        
                        // Health endpoints
                        .requestMatchers("/health", "/actuator/health").permitAll()
                        
                        // All other /api/** endpoints require authentication
                        .requestMatchers("/api/**").authenticated()
                        
                        // Allow all other requests (for non-API endpoints)
                        .anyRequest().permitAll())
                
                // Add JWT authentication filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

