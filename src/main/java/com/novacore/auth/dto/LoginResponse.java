package com.novacore.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

/**
 * Response DTO for successful login.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

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














