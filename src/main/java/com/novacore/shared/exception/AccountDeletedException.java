package com.novacore.shared.exception;

import com.novacore.shared.constants.ErrorCode;

/**
 * Exception thrown when an account is deleted.
 */
public class AccountDeletedException extends BusinessException {

    public AccountDeletedException() {
        super(ErrorCode.AUTH_403_ACCOUNT_DELETED);
    }

    public AccountDeletedException(String message) {
        super(ErrorCode.AUTH_403_ACCOUNT_DELETED, message);
    }

    public AccountDeletedException(String message, Throwable cause) {
        super(ErrorCode.AUTH_403_ACCOUNT_DELETED, message, cause);
    }
}














