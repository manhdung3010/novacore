package com.novacore.auth.service;

import com.novacore.auth.domain.RefreshToken;
import com.novacore.shared.constants.Channel;
import com.novacore.user.domain.User;

import java.util.Optional;

/**
 * Service for refresh token management and session control.
 */
public interface RefreshTokenService {

    /**
     * Creates a new refresh token for a user.
     * 
     * @param user the user entity
     * @param deviceId optional device identifier
     * @param channel the channel (WEB, MOBILE, INTERNAL)
     * @return created RefreshToken entity
     */
    RefreshToken createRefreshToken(User user, String deviceId, Channel channel);

    /**
     * Finds a refresh token by token string.
     * 
     * @param token the refresh token string
     * @return Optional containing RefreshToken if found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Validates a refresh token and returns it if valid.
     * 
     * @param token the refresh token string
     * @return RefreshToken entity if valid
     * @throws com.novacore.shared.exception.BusinessException if token is invalid, expired, or revoked
     */
    RefreshToken validateRefreshToken(String token);

    /**
     * Revokes a refresh token by marking it as revoked.
     * 
     * @param token the refresh token string
     */
    void revokeRefreshToken(String token);

    /**
     * Revokes all active refresh tokens for a user.
     * 
     * @param userId the user ID
     */
    void revokeAllUserTokens(Long userId);

    /**
     * Counts the number of active sessions for a user.
     * 
     * @param userId the user ID
     * @return count of active refresh tokens
     */
    int countActiveSessions(Long userId);

    /**
     * Revokes the oldest session if the user exceeds the maximum session limit.
     * 
     * @param userId the user ID
     * @param maxSessions maximum allowed sessions
     */
    void revokeOldestSessionIfExceedsLimit(Long userId, int maxSessions);

    /**
     * Cleans up expired and revoked tokens from the database.
     * 
     * @return total number of tokens deleted (expired + revoked)
     */
    int cleanupExpiredTokens();
}

