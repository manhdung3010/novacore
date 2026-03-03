package com.novacore.config;

import com.novacore.auth.security.JwtAuthenticationFilter;
import com.novacore.config.properties.AuthProperties;
import com.novacore.config.properties.JwtProperties;
import com.novacore.shared.constants.ApiConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.web.cors.CorsUtils;


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
                        .requestMatchers(
                                ApiConstants.AUTH_ENDPOINT + "/register",
                                ApiConstants.AUTH_ENDPOINT + "/login",
                                ApiConstants.AUTH_ENDPOINT + "/refresh",
                                ApiConstants.AUTH_ENDPOINT + "/logout"
                        ).permitAll()
                        
                        // Health endpoints
                        .requestMatchers("/health", "/actuator/health").permitAll()

                        // WebSocket STOMP endpoint (authentication handled at message level)
                        .requestMatchers("/ws", "/ws/**").permitAll()

                        // Resolve invite by code (GET only - public preview; POST /accept requires auth)
                        .requestMatchers(HttpMethod.GET, "/api/v1/invites/*").permitAll()

                        // Swagger / OpenAPI (dev only in prod via springdoc config)
                        .requestMatchers("/api-docs", "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        
                        // All other /api/** endpoints require authentication
                        .requestMatchers("/api/**").authenticated()
                        
                        // Allow all other requests (for non-API endpoints)
                        .anyRequest().permitAll())
                // Exception handling:
                // - 401 for missing/invalid/expired token (authentication failure)
                // - 403 for authenticated user without required authority
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(org.springframework.http.HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler(accessDeniedHandler())
                )
                // Add JWT authentication filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * AccessDeniedHandler that keeps 403 for authorized but forbidden access.
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        AccessDeniedHandlerImpl handler = new AccessDeniedHandlerImpl();
        handler.setErrorPage(null); // return 403 JSON instead of redirect
        return handler;
    }
}

