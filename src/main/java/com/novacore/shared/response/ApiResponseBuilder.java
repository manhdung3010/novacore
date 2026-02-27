package com.novacore.shared.response;

import com.novacore.shared.constants.ErrorCode;
import com.novacore.shared.context.RequestContextHolder;

import java.time.LocalDateTime;

public class ApiResponseBuilder {

    public static <T> ApiResponse<T> success(T data) {
        return success(ErrorCode.SUCCESS_200_OK.getDefaultMessage(), data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        if (message == null || message.isBlank()) {
            message = ErrorCode.SUCCESS_200_OK.getDefaultMessage();
        }
        
        return ApiResponse.<T>builder()
                .success(true)
                .code(ErrorCode.SUCCESS_200_OK.getCode())
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .requestId(getRequestId())
                .path(getRequestPath())
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return success(message, null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return error(errorCode, errorCode.getDefaultMessage(), null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return error(errorCode, message, null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message, String errorDetails) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(errorCode.getCode())
                .message(message)
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .requestId(getRequestId())
                .path(getRequestPath())
                .build();
    }

    private static String getRequestId() {
        String requestId = RequestContextHolder.getRequestId();
        if (requestId == null) {
            return null;
        }
        return requestId;
    }

    private static String getRequestPath() {
        var context = RequestContextHolder.get();
        return context != null ? context.getPath() : null;
    }
}


















