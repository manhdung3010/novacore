package com.novacore.shared.exception;

import com.novacore.shared.constants.ErrorCode;

/**
 * Exception thrown when an account is locked.
 */
public class AccountLockedException extends BusinessException {

    public AccountLockedException() {
        super(ErrorCode.AUTH_403_ACCOUNT_LOCKED);
    }

    public AccountLockedException(String message) {
        super(ErrorCode.AUTH_403_ACCOUNT_LOCKED, message);
    }

    public AccountLockedException(String message, Throwable cause) {
        super(ErrorCode.AUTH_403_ACCOUNT_LOCKED, message, cause);
    }
}














