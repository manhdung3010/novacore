package com.novacore.auth.service.impl;

import com.novacore.auth.service.TokenService;
import com.novacore.config.properties.JwtProperties;
import com.novacore.shared.constants.Channel;
import com.novacore.shared.constants.ErrorCode;
import com.novacore.shared.exception.BusinessException;
import com.novacore.user.domain.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of TokenService for JWT operations.
 * 
 * Design principles:
 * - Uses HMAC-SHA256 for signing
 * - Includes user ID, username, and channel in token claims
 * - Handles token expiration and validation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final JwtProperties jwtProperties;

    @Override
    public String generateAccessToken(User user, Channel channel) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User and user ID must not be null");
        }
        if (channel == null) {
            channel = Channel.WEB; // Default channel
        }

        Instant now = Instant.now();
        Instant expiration = now.plus(jwtProperties.getAccessTokenTtl());

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("channel", channel.name());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public Claims validateAccessToken(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException(
                ErrorCode.AUTH_401_INVALID_TOKEN,
                "Token is required"
            );
        }

        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.debug("Token expired: {}", e.getMessage());
            throw new BusinessException(
                ErrorCode.AUTH_401_TOKEN_EXPIRED,
                "Token has expired"
            );
        } catch (JwtException e) {
            log.debug("Invalid token: {}", e.getMessage());
            throw new BusinessException(
                ErrorCode.AUTH_401_INVALID_TOKEN,
                "Invalid token: " + e.getMessage()
            );
        }
    }

    @Override
    public Long getUserIdFromToken(String token) {
        Claims claims = validateAccessToken(token);
        Object userIdObj = claims.get("userId");
        
        if (userIdObj == null) {
            throw new BusinessException(
                ErrorCode.AUTH_401_INVALID_TOKEN,
                "Token does not contain user ID"
            );
        }
        
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        
        try {
            return Long.parseLong(userIdObj.toString());
        } catch (NumberFormatException e) {
            throw new BusinessException(
                ErrorCode.AUTH_401_INVALID_TOKEN,
                "Invalid user ID in token"
            );
        }
    }

    @Override
    public Channel getChannelFromToken(String token) {
        Claims claims = validateAccessToken(token);
        Object channelObj = claims.get("channel");
        
        if (channelObj == null) {
            return Channel.WEB; // Default channel
        }
        
        return Channel.from(channelObj.toString(), Channel.WEB);
    }

    @Override
    public boolean isTokenExpired(String token) {
        try {
            validateAccessToken(token);
            return false;
        } catch (BusinessException e) {
            if (ErrorCode.AUTH_401_TOKEN_EXPIRED.equals(e.getErrorCodeEnum())) {
                return true;
            }
            // For other errors, consider token invalid (not just expired)
            return false;
        }
    }

    /**
     * Gets the signing key from JWT secret.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}














