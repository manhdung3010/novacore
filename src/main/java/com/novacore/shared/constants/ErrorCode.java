package com.novacore.shared.constants;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    
    SUCCESS_200_OK("SUCCESS_200_OK", HttpStatus.OK, "Operation completed successfully"),
    
    RESOURCE_404_NOT_FOUND("RESOURCE_404_NOT_FOUND", HttpStatus.NOT_FOUND, "Resource not found"),
    
    VAL_400_VALIDATION_ERROR("VAL_400_VALIDATION_ERROR", HttpStatus.BAD_REQUEST, "Validation failed"),
    VAL_400_ILLEGAL_ARGUMENT("VAL_400_ILLEGAL_ARGUMENT", HttpStatus.BAD_REQUEST, "Invalid argument"),
    VAL_400_BAD_REQUEST("VAL_400_BAD_REQUEST", HttpStatus.BAD_REQUEST, "Bad request"),
    
    AUTH_401_UNAUTHORIZED("AUTH_401_UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Authentication failed"),
    AUTH_401_INVALID_CREDENTIALS("AUTH_401_INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED, "Invalid username or password"),
    AUTH_401_INVALID_TOKEN("AUTH_401_INVALID_TOKEN", HttpStatus.UNAUTHORIZED, "Invalid or expired token"),
    AUTH_401_EXPIRED_TOKEN("AUTH_401_EXPIRED_TOKEN", HttpStatus.UNAUTHORIZED, "Token has expired"),
    AUTH_401_REVOKED_TOKEN("AUTH_401_REVOKED_TOKEN", HttpStatus.UNAUTHORIZED, "Token has been revoked"),
    AUTH_401_TOKEN_EXPIRED("AUTH_401_TOKEN_EXPIRED", HttpStatus.UNAUTHORIZED, "Token has expired"),
    AUTH_401_ACCOUNT_LOCKED("AUTH_401_ACCOUNT_LOCKED", HttpStatus.UNAUTHORIZED, "Account is locked"),
    AUTH_401_ACCOUNT_INACTIVE("AUTH_401_ACCOUNT_INACTIVE", HttpStatus.UNAUTHORIZED, "Account is inactive"),
    AUTH_401_REFRESH_TOKEN_INVALID("AUTH_401_REFRESH_TOKEN_INVALID", HttpStatus.UNAUTHORIZED, "Invalid or revoked refresh token"),
    AUTH_403_FORBIDDEN("AUTH_403_FORBIDDEN", HttpStatus.FORBIDDEN, "Access denied"),
    AUTH_403_ACCOUNT_LOCKED("AUTH_403_ACCOUNT_LOCKED", HttpStatus.FORBIDDEN, "Account is locked"),
    AUTH_403_ACCOUNT_SUSPENDED("AUTH_403_ACCOUNT_SUSPENDED", HttpStatus.FORBIDDEN, "Account is suspended"),
    AUTH_403_ACCOUNT_DELETED("AUTH_403_ACCOUNT_DELETED", HttpStatus.FORBIDDEN, "Account is deleted"),
    
    BUSINESS_400_ERROR("BUSINESS_400_ERROR", HttpStatus.BAD_REQUEST, "Business rule violation"),
    BUSINESS_400_INVALID_PASSWORD("BUSINESS_400_INVALID_PASSWORD", HttpStatus.BAD_REQUEST, "Invalid current password"),
    
    USER_400_USERNAME_EXISTS("USER_400_USERNAME_EXISTS", HttpStatus.BAD_REQUEST, "Username already exists"),
    USER_400_EMAIL_EXISTS("USER_400_EMAIL_EXISTS", HttpStatus.BAD_REQUEST, "Email already exists"),
    USER_404_NOT_FOUND("USER_404_NOT_FOUND", HttpStatus.NOT_FOUND, "User not found"),

    SERVER_404_INVITE_NOT_FOUND("SERVER_404_INVITE_NOT_FOUND", HttpStatus.NOT_FOUND, "Invite not found or invalid"),
    SERVER_400_INVITE_EXPIRED("SERVER_400_INVITE_EXPIRED", HttpStatus.BAD_REQUEST, "Invite has expired or reached max uses"),
    SERVER_409_ALREADY_MEMBER("SERVER_409_ALREADY_MEMBER", HttpStatus.CONFLICT, "Already a member of this server"),

    SYS_500_INTERNAL_ERROR("SYS_500_INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    
    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
    
    ErrorCode(String code, HttpStatus httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
    
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
    
    public String getDefaultMessage() {
        return defaultMessage;
    }
    
    @Deprecated(since = "1.0.0", forRemoval = true)
    public String getMessage() {
        return defaultMessage;
    }
    
    @Deprecated(since = "1.0.0", forRemoval = true)
    public static final ErrorCode SUCCESS = SUCCESS_200_OK;
}

