package com.novacore.auth.service;

import com.novacore.auth.dto.ChangePasswordRequest;
import com.novacore.auth.dto.LoginRequest;
import com.novacore.auth.dto.LoginResponse;
import com.novacore.auth.dto.RefreshRequest;
import com.novacore.auth.dto.RefreshResponse;
import com.novacore.auth.dto.RegisterRequest;
import com.novacore.auth.dto.RegisterResponse;
import com.novacore.user.domain.User;

/**
 * Service for authentication orchestration.
 * Coordinates TokenService, PasswordService, and RefreshTokenService.
 */
public interface AuthService {

    /**
     * Registers a new user account.
     * 
     * @param request registration request containing user information
     * @return RegisterResponse with user info and authentication tokens
     */
    RegisterResponse register(RegisterRequest request);

    /**
     * Authenticates a user and creates a session.
     * 
     * @param request login request containing credentials
     * @return LoginResponse with access and refresh tokens
     */
    LoginResponse login(LoginRequest request);

    /**
     * Refreshes an access token using a refresh token.
     * 
     * @param request refresh request containing refresh token
     * @return RefreshResponse with new access token
     */
    RefreshResponse refreshToken(RefreshRequest request);

    /**
     * Logs out a user by revoking a specific refresh token.
     * 
     * @param refreshToken the refresh token to revoke
     */
    void logout(String refreshToken);

    /**
     * Logs out a user from all sessions.
     * 
     * @param userId the user ID
     */
    void logoutAll(Long userId);

    /**
     * Changes a user's password.
     * 
     * @param userId the user ID
     * @param request change password request
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    /**
     * Locks a user account.
     * 
     * @param user the user entity
     */
    void lockAccount(User user);

    /**
     * Unlocks a user account.
     * 
     * @param user the user entity
     */
    void unlockAccount(User user);

    /**
     * Increments failed login attempts for a user.
     * 
     * @param user the user entity
     */
    void incrementFailedLoginAttempts(User user);

    /**
     * Resets failed login attempts for a user.
     * 
     * @param user the user entity
     */
    void resetFailedLoginAttempts(User user);
}

