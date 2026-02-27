package com.novacore.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Configuration properties for JWT settings.
 * Prefix: jwt
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Secret key used for signing JWT tokens (HMAC).
     * Should be strong enough for security (>= 256 bits).
     */
    @NotBlank(message = "JWT secret must not be blank")
    @Size(min = 32, message = "JWT secret must be at least 32 characters")
    private String secret;

    /**
     * Access token time-to-live.
     * Default: 15 minutes
     */
    private Duration accessTokenTtl = Duration.ofMinutes(15);

    /**
     * Refresh token time-to-live.
     * Default: 7 days
     */
    private Duration refreshTokenTtl = Duration.ofDays(7);

    public boolean isAccessTokenTtlValid() {
        return accessTokenTtl != null
                && !accessTokenTtl.isNegative()
                && !accessTokenTtl.isZero();
    }

    public boolean isRefreshTokenTtlValid() {
        return refreshTokenTtl != null
                && !refreshTokenTtl.isNegative()
                && !refreshTokenTtl.isZero();
    }
}













