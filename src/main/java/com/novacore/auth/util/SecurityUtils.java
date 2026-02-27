package com.novacore.auth.util;

import com.novacore.auth.service.TokenService;
import com.novacore.shared.constants.ErrorCode;
import com.novacore.shared.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Utility class for security-related operations.
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;

    /**
     * Gets the current authenticated user ID from SecurityContext.
     * Extracts user ID from JWT token in the request.
     * 
     * @return user ID
     * @throws BusinessException if user is not authenticated
     */
    public Long getCurrentUserId() {
        try {
            // Try to get token from current request
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                String token = extractTokenFromRequest(request);
                if (token != null) {
                    return tokenService.getUserIdFromToken(token);
                }
            }
        } catch (Exception e) {
            // Fall through to check SecurityContext
        }

        // Fallback: check if we have authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(
                ErrorCode.AUTH_401_UNAUTHORIZED,
                "User is not authenticated"
            );
        }

        throw new BusinessException(
            ErrorCode.AUTH_401_UNAUTHORIZED,
            "Unable to extract user ID from authentication"
        );
    }

    /**
     * Gets the current HTTP request.
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * Extracts JWT token from Authorization header.
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }

    /**
     * Gets the current authenticated username from SecurityContext.
     * 
     * @return username
     * @throws BusinessException if user is not authenticated
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(
                ErrorCode.AUTH_401_UNAUTHORIZED,
                "User is not authenticated"
            );
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        
        if (principal instanceof String) {
            return (String) principal;
        }
        
        throw new BusinessException(
            ErrorCode.AUTH_401_UNAUTHORIZED,
            "Invalid authentication principal"
        );
    }

    /**
     * Checks if user is authenticated.
     * 
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}

