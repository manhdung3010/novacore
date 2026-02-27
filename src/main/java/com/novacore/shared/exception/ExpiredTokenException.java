package com.novacore.shared.exception;

import com.novacore.shared.constants.ErrorCode;

/**
 * Exception thrown when a token has expired.
 */
public class ExpiredTokenException extends BusinessException {

    public ExpiredTokenException() {
        super(ErrorCode.AUTH_401_EXPIRED_TOKEN);
    }

    public ExpiredTokenException(String message) {
        super(ErrorCode.AUTH_401_EXPIRED_TOKEN, message);
    }

    public ExpiredTokenException(String message, Throwable cause) {
        super(ErrorCode.AUTH_401_EXPIRED_TOKEN, message, cause);
    }
}














