package com.novacore.config;

import com.novacore.auth.service.TokenService;
import com.novacore.auth.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket Configuration for STOMP messaging.
 * 
 * - Endpoint: /ws
 * - Protocol: STOMP 1.0/1.1
 * - Fallback: SockJS (for older browsers)
 * - Message prefix: /app (for client->server)
 * - Broker prefix: /topic (for server->client)
 * - JWT token: Via query parameter ?token=... or Authorization header
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final TokenService tokenService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new JwtHandshakeInterceptor(tokenService, userDetailsService))
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic");
    }

    /**
     * Interceptor to extract JWT token from query parameter or header.
     * Sets authentication in SecurityContext for WebSocket requests.
     */
    @Slf4j
    public static class JwtHandshakeInterceptor implements HandshakeInterceptor {

        private final TokenService tokenService;
        private final UserDetailsServiceImpl userDetailsService;

        public JwtHandshakeInterceptor(TokenService tokenService, UserDetailsServiceImpl userDetailsService) {
            this.tokenService = tokenService;
            this.userDetailsService = userDetailsService;
        }

        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                      WebSocketHandler wsHandler, Map<String, Object> attributes) {
            try {
                String token = extractToken(request);

                if (token != null && !token.isEmpty()) {
                    // Validate token
                    tokenService.validateAccessToken(token);

                    // Extract user ID from token
                    Long userId = tokenService.getUserIdFromToken(token);

                    // Load user details
                    UserDetails userDetails = userDetailsService.loadUserById(userId);

                    // Create authentication
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // Set in SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // Store in attributes for later use
                    attributes.put("userId", userId);
                    attributes.put("username", userDetails.getUsername());

                    log.debug("✅ WebSocket authenticated for user: {}", userDetails.getUsername());
                    return true;
                }

                log.warn("⚠️ WebSocket connection attempted without valid token");
                return false;

            } catch (Exception e) {
                log.error("❌ WebSocket authentication failed: {}", e.getMessage());
                SecurityContextHolder.clearContext();
                return false;
            }
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Exception exception) {
            // No-op
        }

        /**
         * Extract JWT token from:
         * 1. Query parameter: ?token=<jwt>
         * 2. Authorization header: Authorization: Bearer <jwt>
         */
        private String extractToken(ServerHttpRequest request) {
            // Try query parameter first
            String query = request.getURI().getQuery();
            if (query != null && query.contains("token=")) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("token=")) {
                        return param.substring(6); // Remove "token=" prefix
                    }
                }
            }

            // Try Authorization header
            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7); // Remove "Bearer " prefix
            }

            return null;
        }
    }
}

