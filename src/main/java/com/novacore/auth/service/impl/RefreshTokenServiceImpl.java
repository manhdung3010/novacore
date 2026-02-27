package com.novacore.auth.service.impl;

import com.novacore.auth.domain.RefreshToken;
import com.novacore.auth.repository.RefreshTokenRepository;
import com.novacore.auth.service.RefreshTokenService;
import com.novacore.config.properties.AuthProperties;
import com.novacore.config.properties.JwtProperties;
import com.novacore.shared.constants.Channel;
import com.novacore.shared.constants.ErrorCode;
import com.novacore.shared.exception.BusinessException;
import com.novacore.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of RefreshTokenService for session management.
 * 
 * Design principles:
 * - Generates cryptographically secure random tokens
 * - Manages token lifecycle (creation, validation, revocation)
 * - Enforces session limits per user
 * - Provides cleanup of expired tokens
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final AuthProperties authProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public RefreshToken createRefreshToken(User user, String deviceId, Channel channel) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User and user ID must not be null");
        }
        if (channel == null) {
            channel = Channel.WEB; // Default channel
        }

        // Generate secure random token
        String token = generateSecureToken();

        // Calculate expiration
        LocalDateTime expiresAt = LocalDateTime.now().plus(jwtProperties.getRefreshTokenTtl());

        // Create refresh token entity
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .deviceId(deviceId)
                .channel(channel)
                .expiresAt(expiresAt)
                .revokedAt(null)
                .build();

        // Check and enforce session limit if configured
        if (authProperties.getMaxSessionsPerUser() != null) {
            revokeOldestSessionIfExceedsLimit(user.getId(), authProperties.getMaxSessionsPerUser());
        }

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return refreshTokenRepository.findByTokenAndRevokedAtIsNull(token);
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException(
                ErrorCode.AUTH_401_REFRESH_TOKEN_INVALID,
                "Refresh token is required"
            );
        }

        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndRevokedAtIsNull(token)
                .orElseThrow(() -> new BusinessException(
                    ErrorCode.AUTH_401_REFRESH_TOKEN_INVALID,
                    "Refresh token not found or has been revoked"
                ));

        if (refreshToken.isExpired()) {
            throw new BusinessException(
                ErrorCode.AUTH_401_TOKEN_EXPIRED,
                "Refresh token has expired"
            );
        }

        if (!refreshToken.isValid()) {
            throw new BusinessException(
                ErrorCode.AUTH_401_REFRESH_TOKEN_INVALID,
                "Refresh token is invalid"
            );
        }

        return refreshToken;
    }

    @Override
    public void revokeRefreshToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        refreshTokenRepository.findByTokenAndRevokedAtIsNull(token)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevokedAt(LocalDateTime.now());
                    refreshTokenRepository.save(refreshToken);
                    log.debug("Revoked refresh token for user ID: {}", refreshToken.getUser().getId());
                });
    }

    @Override
    public void revokeAllUserTokens(Long userId) {
        if (userId == null) {
            return;
        }

        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserIdAndRevokedAtIsNull(userId);
        LocalDateTime now = LocalDateTime.now();
        
        activeTokens.forEach(token -> {
            token.setRevokedAt(now);
            refreshTokenRepository.save(token);
        });

        log.debug("Revoked {} refresh tokens for user ID: {}", activeTokens.size(), userId);
    }

    @Override
    @Transactional(readOnly = true)
    public int countActiveSessions(Long userId) {
        if (userId == null) {
            return 0;
        }
        return (int) refreshTokenRepository.countByUserIdAndRevokedAtIsNull(userId);
    }

    @Override
    public void revokeOldestSessionIfExceedsLimit(Long userId, int maxSessions) {
        if (userId == null || maxSessions <= 0) {
            return;
        }

        // Count active sessions for the user
        // SQL: SELECT COUNT(*) FROM refresh_tokens WHERE user_id = :userId AND revoked_at IS NULL
        long activeCount = refreshTokenRepository.countByUserIdAndRevokedAtIsNull(userId);
        
        // If user already has max sessions, revoke the oldest ones before creating new token
        // Example: maxSessions = 5, activeCount = 5, we need to revoke 1 to make room for new token
        if (activeCount >= maxSessions) {
            // Get all tokens ordered by creation date (oldest first)
            // SQL: SELECT * FROM refresh_tokens WHERE user_id = :userId ORDER BY created_at ASC
            List<RefreshToken> allTokens = refreshTokenRepository.findByUserIdOrderByCreatedAtAsc(userId);
            
            // Calculate how many tokens to revoke
            // If activeCount = 5 and maxSessions = 5, we need to revoke 1 to make room for new token
            int tokensToRevoke = (int) (activeCount - maxSessions + 1);
            LocalDateTime now = LocalDateTime.now();
            
            int revoked = 0;
            for (RefreshToken token : allTokens) {
                // Only revoke active (non-revoked) tokens
                if (token.getRevokedAt() == null && revoked < tokensToRevoke) {
                    token.setRevokedAt(now);
                    refreshTokenRepository.save(token);
                    revoked++;
                    log.debug("Revoked oldest session (tokenId={}, createdAt={}) for user ID: {}", 
                        token.getId(), token.getCreatedAt(), userId);
                }
            }
            
            log.info("Revoked {} oldest session(s) for user ID: {} to enforce limit of {} (had {} active sessions)", 
                revoked, userId, maxSessions, activeCount);
        }
    }

    @Override
    public int cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        
        // 1. Cleanup expired tokens
        // SQL: DELETE FROM refresh_tokens WHERE expires_at < NOW()
        long expiredCount = refreshTokenRepository.countExpiredTokens(now);
        refreshTokenRepository.deleteByExpiresAtBefore(now);
        
        // 2. Cleanup revoked tokens
        // SQL: DELETE FROM refresh_tokens WHERE revoked_at IS NOT NULL
        long revokedCount = refreshTokenRepository.countRevokedTokens();
        refreshTokenRepository.deleteRevokedTokens();
        
        int totalDeleted = (int) (expiredCount + revokedCount);
        
        if (totalDeleted > 0) {
            log.info("Cleanup completed: {} expired tokens and {} revoked tokens deleted (total: {})", 
                expiredCount, revokedCount, totalDeleted);
        } else {
            log.debug("No tokens to clean up (expired: {}, revoked: {})", expiredCount, revokedCount);
        }
        
        return totalDeleted;
    }

    /**
     * Generates a cryptographically secure random token.
     * Uses SecureRandom to generate 32 bytes (256 bits) of random data,
     * then Base64 encodes it for storage.
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[32]; // 256 bits
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}

