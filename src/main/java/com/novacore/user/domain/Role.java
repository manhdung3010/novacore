package com.novacore.user.domain;

/**
 * User role enumeration.
 * 
 * Design principles:
 * - Defines available user roles in the system
 * - Used for authorization and access control
 */
public enum Role {
    /**
     * Regular user role - default role for all users.
     */
    USER,
    
    /**
     * Administrator role - full system access.
     */
    ADMIN,
    
    /**
     * Moderator role - limited administrative access.
     */
    MODERATOR;
    
    /**
     * Converts string to Role enum.
     * 
     * @param value the string value
     * @return Role enum, defaults to USER if invalid
     */
    public static Role from(String value) {
        if (value == null || value.isBlank()) {
            return USER;
        }
        
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return USER;
        }
    }
    
    /**
     * Converts string to Role enum with default value.
     * 
     * @param value the string value
     * @param defaultValue the default role if value is invalid
     * @return Role enum
     */
    public static Role from(String value, Role defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }
    
    /**
     * Gets the Spring Security authority string (ROLE_*).
     * 
     * @return authority string (e.g., "ROLE_USER", "ROLE_ADMIN")
     */
    public String getAuthority() {
        return "ROLE_" + name();
    }
}














