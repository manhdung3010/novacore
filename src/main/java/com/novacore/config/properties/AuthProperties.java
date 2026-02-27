package com.novacore.config.properties;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Configuration properties for authentication settings.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    /**
     * Maximum number of concurrent sessions allowed per user.
     * Optional: If null, no limit is enforced.
     */
    @Min(value = 1, message = "Max sessions per user must be at least 1 if specified")
    private Integer maxSessionsPerUser;

    /**
     * Duration for which an account is locked after failed login attempts.
     * Optional: If null, account locking is disabled.
     */
    private Duration accountLockDuration;

    @AssertTrue(message = "Account lock duration must be positive if specified")
    public boolean isAccountLockDurationValid() {
        return accountLockDuration == null
                || (!accountLockDuration.isNegative() && !accountLockDuration.isZero());
    }
}













