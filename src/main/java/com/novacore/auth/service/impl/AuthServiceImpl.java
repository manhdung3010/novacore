package com.novacore.auth.service.impl;

import com.novacore.auth.domain.RefreshToken;
import com.novacore.auth.dto.ChangePasswordRequest;
import com.novacore.auth.dto.LoginRequest;
import com.novacore.auth.dto.LoginResponse;
import com.novacore.auth.dto.RefreshRequest;
import com.novacore.auth.dto.RefreshResponse;
import com.novacore.auth.dto.RegisterRequest;
import com.novacore.auth.dto.RegisterResponse;
import com.novacore.auth.service.AuthService;
import com.novacore.auth.service.PasswordService;
import com.novacore.auth.service.RefreshTokenService;
import com.novacore.auth.service.TokenService;
import com.novacore.config.properties.AuthProperties;
import com.novacore.config.properties.JwtProperties;
import com.novacore.shared.constants.Channel;
import com.novacore.shared.constants.ErrorCode;
import com.novacore.shared.exception.BusinessException;
import com.novacore.shared.exception.ResourceNotFoundException;
import com.novacore.user.domain.Role;
import com.novacore.user.domain.User;
import com.novacore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of AuthService for authentication orchestration.
 * 
 * Design principles:
 * - Orchestrates TokenService, PasswordService, and RefreshTokenService
 * - Handles business logic for login, logout, password change
 * - Manages account locking and failed login attempts
 * - Validates user status and account state before operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordService passwordService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties;
    private final AuthProperties authProperties;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        // 1. Validate input
        if (request == null) {
            throw new BusinessException(
                ErrorCode.VAL_400_BAD_REQUEST,
                "Registration request is required"
            );
        }

        // 2. Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(
                ErrorCode.VAL_400_VALIDATION_ERROR,
                "Password and confirm password do not match"
            );
        }

        // 3. Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(
                ErrorCode.USER_400_USERNAME_EXISTS,
                String.format("Username '%s' already exists", request.getUsername())
            );
        }

        // 4. Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                ErrorCode.USER_400_EMAIL_EXISTS,
                String.format("Email '%s' already exists", request.getEmail())
            );
        }

        // 5. Hash password
        String hashedPassword = passwordService.encodePassword(request.getPassword());

        // 6. Create user entity
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .password(hashedPassword)
                .status(User.UserStatus.ACTIVE)
                .role(Role.USER) // Default role for new registrations
                .failedLoginAttempts(0)
                .build();

        // 7. Save user
        User savedUser = userRepository.save(user);
        log.info("New user registered: username={}, email={}, userId={}", 
            savedUser.getUsername(), savedUser.getEmail(), savedUser.getId());

        // 8. Determine channel (default to WEB)
        Channel channel = Channel.WEB;

        // 9. Create session (refresh token)
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
            savedUser,
            null, // No device ID for registration
            channel
        );

        // 10. Generate access token
        String accessToken = tokenService.generateAccessToken(savedUser, channel);

        // 11. Build response
        return RegisterResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenTtl().getSeconds())
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .avatar(savedUser.getAvatar())
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. Validate input
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            throw new BusinessException(
                ErrorCode.VAL_400_BAD_REQUEST,
                "Username and password are required"
            );
        }

        // 2. Load user
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(
                    ErrorCode.AUTH_401_INVALID_CREDENTIALS,
                    "Invalid username or password"
                ));

        // 3. Check status/lock
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new BusinessException(
                ErrorCode.AUTH_401_ACCOUNT_INACTIVE,
                "Account is not active"
            );
        }

        if (user.isAccountLocked()) {
            throw new BusinessException(
                ErrorCode.AUTH_401_ACCOUNT_LOCKED,
                "Account is locked. Please try again later"
            );
        }

        // 4. Verify password
        if (!passwordService.matches(request.getPassword(), user.getPassword())) {
            incrementFailedLoginAttempts(user);
            throw new BusinessException(
                ErrorCode.AUTH_401_INVALID_CREDENTIALS,
                "Invalid username or password"
            );
        }

        // 5. Reset failed attempts
        resetFailedLoginAttempts(user);

        // 6. Update lastLoginAt
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 7. Determine channel
        Channel channel = Channel.from(request.getChannel(), Channel.WEB);

        // 8. Create session (refresh token)
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
            user,
            request.getDeviceId(),
            channel
        );

        // 9. Generate tokens
        String accessToken = tokenService.generateAccessToken(user, channel);

        // 10. Build response
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenTtl().getSeconds())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public RefreshResponse refreshToken(RefreshRequest request) {
        // 1. Validate input
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            throw new BusinessException(
                ErrorCode.VAL_400_BAD_REQUEST,
                "Refresh token is required"
            );
        }

        // 2. Find and validate token
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());

        // 3. Load user
        User user = refreshToken.getUser();
        if (user == null) {
            throw new BusinessException(
                ErrorCode.AUTH_401_REFRESH_TOKEN_INVALID,
                "Invalid refresh token"
            );
        }

        // 4. Check ACTIVE status
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            // Revoke the token if user is not active
            refreshTokenService.revokeRefreshToken(refreshToken.getToken());
            throw new BusinessException(
                ErrorCode.AUTH_401_ACCOUNT_INACTIVE,
                "Account is not active"
            );
        }

        // 5. Generate new access token
        String accessToken = tokenService.generateAccessToken(user, refreshToken.getChannel());

        // 6. Build response
        return RefreshResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenTtl().getSeconds())
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        refreshTokenService.revokeRefreshToken(refreshToken);
    }

    @Override
    public void logoutAll(Long userId) {
        if (userId == null) {
            return;
        }
        refreshTokenService.revokeAllUserTokens(userId);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // 1. Validate input
        if (request == null || request.getCurrentPassword() == null || request.getNewPassword() == null) {
            throw new BusinessException(
                ErrorCode.VAL_400_BAD_REQUEST,
                "Current password and new password are required"
            );
        }

        // 2. Validate confirm password matches new password
        if (request.getConfirmPassword() == null || !request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(
                ErrorCode.VAL_400_VALIDATION_ERROR,
                "New password and confirm password do not match"
            );
        }

        // 3. Load user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    ErrorCode.USER_404_NOT_FOUND,
                    "User not found"
                ));

        // 4. Verify current password
        if (!passwordService.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(
                ErrorCode.BUSINESS_400_INVALID_PASSWORD,
                "Current password is incorrect"
            );
        }

        // 5. Update password
        user.setPassword(passwordService.encodePassword(request.getNewPassword()));
        userRepository.save(user);

        // 6. Revoke all tokens (force re-login)
        refreshTokenService.revokeAllUserTokens(userId);

        log.info("Password changed for user ID: {}", userId);
    }

    @Override
    public void lockAccount(User user) {
        if (user == null) {
            return;
        }

        if (authProperties.getAccountLockDuration() != null) {
            LocalDateTime lockUntil = LocalDateTime.now().plus(authProperties.getAccountLockDuration());
            user.setAccountLockedUntil(lockUntil);
            userRepository.save(user);
            log.info("Account locked for user ID: {} until {}", user.getId(), lockUntil);
        }
    }

    @Override
    public void unlockAccount(User user) {
        if (user == null) {
            return;
        }

        user.setAccountLockedUntil(null);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        log.info("Account unlocked for user ID: {}", user.getId());
    }

    @Override
    public void incrementFailedLoginAttempts(User user) {
        if (user == null) {
            return;
        }

        int newAttempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts()) + 1;
        user.setFailedLoginAttempts(newAttempts);

        // Lock account if configured threshold is reached (e.g., 5 attempts)
        // This is a simple implementation - you might want to make this configurable
        int maxAttempts = 5; // Could be moved to AuthProperties
        if (newAttempts >= maxAttempts && authProperties.getAccountLockDuration() != null) {
            lockAccount(user);
        } else {
            userRepository.save(user);
        }

        log.debug("Incremented failed login attempts for user ID: {} to {}", user.getId(), newAttempts);
    }

    @Override
    public void resetFailedLoginAttempts(User user) {
        if (user == null) {
            return;
        }

        if (user.getFailedLoginAttempts() != null && user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
            log.debug("Reset failed login attempts for user ID: {}", user.getId());
        }
    }
}

