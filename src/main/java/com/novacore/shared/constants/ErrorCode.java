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
    AUTH_403_FORBIDDEN("AUTH_403_FORBIDDEN", HttpStatus.FORBIDDEN, "Access denied"),
    
    BUSINESS_400_ERROR("BUSINESS_400_ERROR", HttpStatus.BAD_REQUEST, "Business rule violation"),
    
    USER_400_USERNAME_EXISTS("USER_400_USERNAME_EXISTS", HttpStatus.BAD_REQUEST, "Username already exists"),
    USER_400_EMAIL_EXISTS("USER_400_EMAIL_EXISTS", HttpStatus.BAD_REQUEST, "Email already exists"),
    USER_404_NOT_FOUND("USER_404_NOT_FOUND", HttpStatus.NOT_FOUND, "User not found"),
    
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

