package com.novacore.shared.exception;

import com.novacore.shared.constants.ErrorCode;
import com.novacore.shared.context.RequestContextHolder;
import com.novacore.shared.response.ApiResponse;
import com.novacore.shared.response.ApiResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex) {
        logWithContextWarn("ResourceNotFoundException", ex.getErrorCode(), ex.getMessage());
        
        ApiResponse<Object> response = ApiResponseBuilder.error(
            ex.getErrorCodeEnum(),
            ex.getMessage()
        );
        
        return ResponseEntity.status(ex.getErrorCodeEnum().getHttpStatus()).body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException ex) {
        logWithContextWarn("BusinessException", ex.getErrorCode(), ex.getMessage());
        
        ApiResponse<Object> response = ApiResponseBuilder.error(
            ex.getErrorCodeEnum(),
            ex.getMessage()
        );
        
        return ResponseEntity.status(ex.getErrorCodeEnum().getHttpStatus()).body(response);
    }

    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ApiResponse<Object>> handleSystemException(SystemException ex) {
        logWithContextError("SystemException", ex.getErrorCode(), ex.getMessage(), ex);
        
        ApiResponse<Object> response = ApiResponseBuilder.error(
            ErrorCode.SYS_500_INTERNAL_ERROR,
            "A system error occurred. Please try again later."
        );
        
        return ResponseEntity.status(ErrorCode.SYS_500_INTERNAL_ERROR.getHttpStatus()).body(response);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Object>> handleBaseException(BaseException ex) {
        logWithContextError("BaseException", ex.getErrorCode(), ex.getMessage(), ex);
        
        ApiResponse<Object> response = ApiResponseBuilder.error(
            ErrorCode.SYS_500_INTERNAL_ERROR,
            "An unexpected error occurred. Please try again later."
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        logWithContextWarn("ValidationException", ErrorCode.VAL_400_VALIDATION_ERROR.getCode(), 
                      "Validation failed: " + errors.size() + " field(s)");

        ApiResponse<Map<String, String>> response = ApiResponseBuilder.error(
            ErrorCode.VAL_400_VALIDATION_ERROR,
            "Validation failed"
        );
        response.setData(errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        logWithContextWarn("IllegalArgumentException", ErrorCode.VAL_400_ILLEGAL_ARGUMENT.getCode(), 
                      ex.getMessage());
        
        ApiResponse<Object> response = ApiResponseBuilder.error(
            ErrorCode.VAL_400_ILLEGAL_ARGUMENT,
            ex.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        logWithContextError("UnexpectedException", ErrorCode.SYS_500_INTERNAL_ERROR.getCode(), 
                      "Unexpected error: " + ex.getClass().getSimpleName(), ex);
        
        ApiResponse<Object> response = ApiResponseBuilder.error(
            ErrorCode.SYS_500_INTERNAL_ERROR,
            "An unexpected error occurred. Please try again later."
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private void logWithContextWarn(String exceptionType, String errorCode, String message) {
        String requestId = RequestContextHolder.getRequestId();
        String traceId = RequestContextHolder.getTraceId();
        
        log.warn("[{}] {} | requestId={} | traceId={} | errorCode={}", 
                 exceptionType, message, requestId, traceId, errorCode);
    }

    private void logWithContextError(String exceptionType, String errorCode, String message, Throwable ex) {
        String requestId = RequestContextHolder.getRequestId();
        String traceId = RequestContextHolder.getTraceId();
        
        log.error("[{}] {} | requestId={} | traceId={} | errorCode={}", 
                 exceptionType, message, requestId, traceId, errorCode, ex);
    }
}


















