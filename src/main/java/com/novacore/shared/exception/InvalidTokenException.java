package com.novacore.shared.exception;

import com.novacore.shared.constants.ErrorCode;

/**
 * Exception thrown when a token is invalid (malformed, wrong signature, etc.).
 */
public class InvalidTokenException extends BusinessException {

    public InvalidTokenException() {
        super(ErrorCode.AUTH_401_INVALID_TOKEN);
    }

    public InvalidTokenException(String message) {
        super(ErrorCode.AUTH_401_INVALID_TOKEN, message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(ErrorCode.AUTH_401_INVALID_TOKEN, message, cause);
    }
}














