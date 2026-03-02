package com.novacore.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

/**
 * Response DTO for successful registration.
 * Returns user information and authentication tokens.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {

    private String accessToken;
    private String refreshToken;
    @Default
    private String tokenType = "Bearer";
    private Long expiresIn; // seconds
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String avatar;
}














