package com.novacore.shared.exception;

import com.novacore.shared.constants.ErrorCode;

/**
 * Exception thrown when an account is suspended.
 */
public class AccountSuspendedException extends BusinessException {

    public AccountSuspendedException() {
        super(ErrorCode.AUTH_403_ACCOUNT_SUSPENDED);
    }

    public AccountSuspendedException(String message) {
        super(ErrorCode.AUTH_403_ACCOUNT_SUSPENDED, message);
    }

    public AccountSuspendedException(String message, Throwable cause) {
        super(ErrorCode.AUTH_403_ACCOUNT_SUSPENDED, message, cause);
    }
}














