package com.novacore.auth.util;

import com.novacore.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility class for session cleanup operations.
 * Can be used to enforce session limits on existing users.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionCleanupUtil {

    private final RefreshTokenService refreshTokenService;

    /**
     * Enforces session limit for a specific user.
     * Revokes oldest sessions if user exceeds the limit.
     * 
     * @param userId the user ID
     * @param maxSessions maximum allowed sessions
     * @return number of sessions revoked
     */
    public int enforceSessionLimit(Long userId, int maxSessions) {
        if (userId == null || maxSessions <= 0) {
            return 0;
        }

        int currentActiveSessions = refreshTokenService.countActiveSessions(userId);
        
        if (currentActiveSessions <= maxSessions) {
            log.debug("User ID: {} has {} active sessions, within limit of {}", 
                userId, currentActiveSessions, maxSessions);
            return 0;
        }

        // Calculate how many to revoke
        int sessionsToRevoke = currentActiveSessions - maxSessions;
        
        // Use the existing method to revoke oldest sessions
        refreshTokenService.revokeOldestSessionIfExceedsLimit(userId, maxSessions);
        
        log.info("Enforced session limit for user ID: {} - had {} sessions, limit is {}, revoked {} sessions", 
            userId, currentActiveSessions, maxSessions, sessionsToRevoke);
        
        return sessionsToRevoke;
    }
}














