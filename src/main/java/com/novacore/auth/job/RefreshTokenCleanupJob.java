package com.novacore.auth.job;

import com.novacore.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job for cleaning up expired refresh tokens.
 * 
 * Design principles:
 * - Runs periodically to remove expired tokens from database
 * - Configurable schedule (default: every hour)
 * - Logs cleanup statistics
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupJob {

    private final RefreshTokenService refreshTokenService;

    /**
     * Cleans up expired and revoked refresh tokens.
     * 
     * Schedule options (configurable via application.yml):
     * - Fixed delay: auth.token-cleanup-interval (milliseconds) - default: 3600000 (1 hour)
     * - Cron expression: auth.token-cleanup-cron - e.g., "0 0 * * * ?" (every hour)
     * 
     * SQL executed:
     * 1. DELETE FROM refresh_tokens WHERE expires_at < NOW()  (expired tokens)
     * 2. DELETE FROM refresh_tokens WHERE revoked_at IS NOT NULL  (revoked tokens)
     * 
     * This job helps maintain database stability by removing expired and revoked tokens.
     */
    @Scheduled(
        fixedDelayString = "${auth.token-cleanup-interval:360000}", 
        initialDelayString = "${auth.token-cleanup-initial-delay:60000}"
    )
    public void cleanupExpiredTokens() {
        try {
            log.debug("Starting refresh token cleanup job");
            int deletedCount = refreshTokenService.cleanupExpiredTokens();
            
            if (deletedCount > 0) {
                log.info("Refresh token cleanup completed: {} tokens deleted (expired + revoked)", deletedCount);
            } else {
                log.debug("Refresh token cleanup completed: no tokens to clean up");
            }
        } catch (Exception e) {
            log.error("Error during refresh token cleanup", e);
        }
    }
    
    /**
     * Alternative cleanup method using cron expression.
     * Uncomment and configure auth.token-cleanup-cron to use this instead of fixedDelay.
     * 
     * Examples:
     * - "0 0 * * * ?" = every hour at minute 0
     * - "0 0 0 * * ?" = every day at midnight
     * - "0 0 0 * * MON" = every Monday at midnight
     */
    // @Scheduled(cron = "${auth.token-cleanup-cron:0 0 * * * ?}")
    // public void cleanupExpiredTokensCron() {
    //     cleanupExpiredTokens();
    // }
}

