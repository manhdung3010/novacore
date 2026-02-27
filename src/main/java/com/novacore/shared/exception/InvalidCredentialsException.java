package com.novacore.shared.exception;

import com.novacore.shared.constants.ErrorCode;

/**
 * Exception thrown when authentication credentials are invalid.
 */
public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super(ErrorCode.AUTH_401_INVALID_CREDENTIALS);
    }

    public InvalidCredentialsException(String message) {
        super(ErrorCode.AUTH_401_INVALID_CREDENTIALS, message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(ErrorCode.AUTH_401_INVALID_CREDENTIALS, message, cause);
    }
}














