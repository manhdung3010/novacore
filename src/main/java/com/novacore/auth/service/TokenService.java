package com.novacore.auth.service;

import com.novacore.shared.constants.Channel;
import com.novacore.user.domain.User;
import io.jsonwebtoken.Claims;

/**
 * Service for JWT token operations.
 */
public interface TokenService {

    /**
     * Generates an access token (JWT) for a user.
     * 
     * @param user the user entity
     * @param channel the channel (WEB, MOBILE, INTERNAL)
     * @return JWT token string
     */
    String generateAccessToken(User user, Channel channel);

    /**
     * Validates and parses an access token.
     * 
     * @param token the JWT token string
     * @return Claims object containing token data
     * @throws com.novacore.shared.exception.BusinessException if token is invalid or expired
     */
    Claims validateAccessToken(String token);

    /**
     * Extracts user ID from a token.
     * 
     * @param token the JWT token string
     * @return user ID
     * @throws com.novacore.shared.exception.BusinessException if token is invalid
     */
    Long getUserIdFromToken(String token);

    /**
     * Extracts channel from a token.
     * 
     * @param token the JWT token string
     * @return channel enum
     * @throws com.novacore.shared.exception.BusinessException if token is invalid
     */
    Channel getChannelFromToken(String token);

    /**
     * Checks if a token is expired.
     * 
     * @param token the JWT token string
     * @return true if token is expired, false otherwise
     */
    boolean isTokenExpired(String token);
}














