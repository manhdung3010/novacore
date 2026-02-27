package com.novacore.shared.exception;

import com.novacore.shared.constants.ErrorCode;

/**
 * Exception thrown when a token has been revoked.
 */
public class RevokedTokenException extends BusinessException {

    public RevokedTokenException() {
        super(ErrorCode.AUTH_401_REVOKED_TOKEN);
    }

    public RevokedTokenException(String message) {
        super(ErrorCode.AUTH_401_REVOKED_TOKEN, message);
    }

    public RevokedTokenException(String message, Throwable cause) {
        super(ErrorCode.AUTH_401_REVOKED_TOKEN, message, cause);
    }
}














