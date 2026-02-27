package com.novacore.auth.repository;

import com.novacore.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for RefreshToken entity.
 * 
 * Design principles:
 * - Extends JpaRepository for standard CRUD operations
 * - Provides custom query methods for token management
 * - Methods follow Spring Data JPA naming conventions
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Finds a refresh token by token value that has not been revoked.
     * Used for token validation during refresh operations.
     * 
     * @param token the token string to search for
     * @return Optional containing the RefreshToken if found and not revoked
     */
    Optional<RefreshToken> findByTokenAndRevokedAtIsNull(String token);

    /**
     * Finds all active (non-revoked) refresh tokens for a specific user.
     * Used to retrieve all valid sessions for a user.
     * 
     * @param userId the user ID
     * @return List of active refresh tokens for the user
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revokedAt IS NULL")
    List<RefreshToken> findByUserIdAndRevokedAtIsNull(@Param("userId") Long userId);

    /**
     * Counts the number of active (non-revoked) refresh tokens for a specific user.
     * Used to enforce max sessions per user limit.
     * 
     * @param userId the user ID
     * @return count of active refresh tokens
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revokedAt IS NULL")
    long countByUserIdAndRevokedAtIsNull(@Param("userId") Long userId);

    /**
     * Finds all refresh tokens for a user, ordered by creation date (oldest first).
     * Used for session management (e.g., removing oldest sessions when limit is reached).
     * 
     * @param userId the user ID
     * @return List of refresh tokens ordered by createdAt ascending
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId ORDER BY rt.createdAt ASC")
    List<RefreshToken> findByUserIdOrderByCreatedAtAsc(@Param("userId") Long userId);

    /**
     * Deletes all refresh tokens that have expired before the given timestamp.
     * SQL: DELETE FROM refresh_tokens WHERE expires_at < :now
     * Used for cleanup of expired tokens (typically called by scheduled tasks).
     * 
     * @param now the cutoff timestamp (tokens expired before this will be deleted)
     */
    void deleteByExpiresAtBefore(LocalDateTime now);
    
    /**
     * Counts expired tokens before cleanup.
     * SQL: SELECT COUNT(*) FROM refresh_tokens WHERE expires_at < :now
     * 
     * @param now the cutoff timestamp
     * @return count of expired tokens
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.expiresAt < :now")
    long countExpiredTokens(@Param("now") LocalDateTime now);
    
    /**
     * Deletes all revoked tokens.
     * SQL: DELETE FROM refresh_tokens WHERE revoked_at IS NOT NULL
     * Used for cleanup of revoked tokens (typically called by scheduled tasks).
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revokedAt IS NOT NULL")
    void deleteRevokedTokens();
    
    /**
     * Counts revoked tokens before cleanup.
     * SQL: SELECT COUNT(*) FROM refresh_tokens WHERE revoked_at IS NOT NULL
     * 
     * @return count of revoked tokens
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.revokedAt IS NOT NULL")
    long countRevokedTokens();
}

