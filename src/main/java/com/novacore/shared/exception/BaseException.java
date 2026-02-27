package com.novacore.shared.exception;

import com.novacore.shared.constants.ErrorCode;
import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final Object[] messageArgs;

    protected BaseException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.messageArgs = null;
    }

    protected BaseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.messageArgs = null;
    }

    protected BaseException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.messageArgs = null;
    }

    protected BaseException(ErrorCode errorCode, Object... messageArgs) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.messageArgs = messageArgs;
    }

    public String getErrorCode() {
        return errorCode.getCode();
    }

    public ErrorCode getErrorCodeEnum() {
        return errorCode;
    }
}


















