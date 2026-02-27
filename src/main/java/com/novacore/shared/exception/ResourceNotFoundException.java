package com.novacore.shared.exception;

import com.novacore.shared.constants.ErrorCode;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
            ErrorCode.RESOURCE_404_NOT_FOUND,
            String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue)
        );
    }
}


















