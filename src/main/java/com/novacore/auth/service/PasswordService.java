package com.novacore.auth.service;

/**
 * Service for password encoding and verification using BCrypt.
 */
public interface PasswordService {

    /**
     * Encodes a raw password using BCrypt.
     * 
     * @param rawPassword the raw password to encode
     * @return BCrypt hashed password
     */
    String encodePassword(String rawPassword);

    /**
     * Verifies if a raw password matches an encoded password.
     * 
     * @param rawPassword the raw password to verify
     * @param encodedPassword the encoded password to compare against
     * @return true if passwords match, false otherwise
     */
    boolean matches(String rawPassword, String encodedPassword);
}














