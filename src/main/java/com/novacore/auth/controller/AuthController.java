package com.novacore.auth.controller;

import com.novacore.auth.dto.ChangePasswordRequest;
import com.novacore.auth.dto.LoginRequest;
import com.novacore.auth.dto.LoginResponse;
import com.novacore.auth.dto.RefreshRequest;
import com.novacore.auth.dto.RefreshResponse;
import com.novacore.auth.dto.RegisterRequest;
import com.novacore.auth.dto.RegisterResponse;
import com.novacore.auth.service.AuthService;
import com.novacore.auth.util.SecurityUtils;
import com.novacore.shared.response.ApiResponse;
import com.novacore.shared.response.ApiResponseBuilder;
import com.novacore.user.dto.UserDto;
import com.novacore.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication endpoints.
 * 
 * Design principles:
 * - Public endpoints: login, refresh
 * - Protected endpoints: logout, logout-all, change-password
 * - Returns standardized ApiResponse format
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SecurityUtils securityUtils;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseBuilder.success("Registration successful", response));
    }
    

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponseBuilder.success("Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponseBuilder.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponseBuilder.success("Logout successful"));
    }

    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Object>> logoutAll() {
        Long userId = securityUtils.getCurrentUserId();
        authService.logoutAll(userId);
        return ResponseEntity.ok(ApiResponseBuilder.success("Logged out from all sessions"));
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Object>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        authService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponseBuilder.success("Password changed successfully"));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser() {
        Long userId = securityUtils.getCurrentUserId();
        UserDto userDto = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponseBuilder.success("User retrieved successfully", userDto));
    }
}

