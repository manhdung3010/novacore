package com.novacore.auth.security;

import com.novacore.shared.constants.ErrorCode;
import com.novacore.shared.exception.BusinessException;
import com.novacore.user.domain.Role;
import com.novacore.user.domain.User;
import com.novacore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * UserDetailsService implementation for Spring Security.
 * 
 * Design principles:
 * - Loads user from database by username
 * - Validates user status (must be ACTIVE)
 * - Creates UserDetails with authorities
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                    String.format("User not found with username: %s", username)
                ));

        // Check if user is active
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new BusinessException(
                ErrorCode.AUTH_401_ACCOUNT_INACTIVE,
                "Account is not active"
            );
        }

        // Check if account is locked
        if (user.isAccountLocked()) {
            throw new BusinessException(
                ErrorCode.AUTH_401_ACCOUNT_LOCKED,
                "Account is locked"
            );
        }

        // Create authorities from user role
        Role userRole = user.getRole() != null ? user.getRole() : Role.USER;
        var authorities = Collections.singletonList(new SimpleGrantedAuthority(userRole.getAuthority()));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(user.isAccountLocked())
                .credentialsExpired(false)
                .disabled(user.getStatus() != User.UserStatus.ACTIVE)
                .build();
    }

    /**
     * Loads user by ID for JWT authentication.
     * 
     * @param userId the user ID
     * @return UserDetails
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                    String.format("User not found with ID: %d", userId)
                ));

        // Check if user is active
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new BusinessException(
                ErrorCode.AUTH_401_ACCOUNT_INACTIVE,
                "Account is not active"
            );
        }

        // Check if account is locked
        if (user.isAccountLocked()) {
            throw new BusinessException(
                ErrorCode.AUTH_401_ACCOUNT_LOCKED,
                "Account is locked"
            );
        }

        // Create authorities from user role
        Role userRole = user.getRole() != null ? user.getRole() : Role.USER;
        var authorities = Collections.singletonList(new SimpleGrantedAuthority(userRole.getAuthority()));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(user.isAccountLocked())
                .credentialsExpired(false)
                .disabled(user.getStatus() != User.UserStatus.ACTIVE)
                .build();
    }
}

