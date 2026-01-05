package com.novacore.exception.handler;

import com.novacore.shared.constants.ErrorCode;
import com.novacore.shared.response.ApiResponse;
import com.novacore.shared.exception.ResourceNotFoundException;
import com.novacore.shared.exception.BusinessException;
import com.novacore.shared.exception.SystemException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler
 * 
 * Tests exception handling and error code mapping
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private com.novacore.shared.exception.GlobalExceptionHandler exceptionHandler;

    @Test
    void handleResourceNotFoundException_ShouldReturn404() {
        // Given
        ResourceNotFoundException ex = new ResourceNotFoundException("User", "id", 1L);
        
        // When
        ResponseEntity<ApiResponse<Object>> response = 
                exceptionHandler.handleResourceNotFoundException(ex);
        
        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorCode.RESOURCE_404_NOT_FOUND.getCode(), response.getBody().getCode());
    }

    @Test
    void handleBusinessException_ShouldReturn400() {
        // Given
        BusinessException ex = new BusinessException(
                ErrorCode.BUSINESS_400_ERROR,
                "Business rule violation"
        );
        
        // When
        ResponseEntity<ApiResponse<Object>> response = 
                exceptionHandler.handleBusinessException(ex);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorCode.BUSINESS_400_ERROR.getCode(), response.getBody().getCode());
    }

    @Test
    void handleSystemException_ShouldReturn500() {
        // Given
        SystemException ex = new SystemException(
                ErrorCode.SYS_500_INTERNAL_ERROR,
                "Database connection failed"
        );
        
        // When
        ResponseEntity<ApiResponse<Object>> response = 
                exceptionHandler.handleSystemException(ex);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorCode.SYS_500_INTERNAL_ERROR.getCode(), response.getBody().getCode());
    }

    @Test
    void handleValidationExceptions_ShouldReturn400WithFieldErrors() {
        // Given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("createUserRequest", "email", "Email is required"));
        fieldErrors.add(new FieldError("createUserRequest", "name", "Name must not be blank"));
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>(fieldErrors));
        
        // When
        ResponseEntity<ApiResponse<Map<String, String>>> response = 
                exceptionHandler.handleValidationExceptions(ex);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorCode.VAL_400_VALIDATION_ERROR.getCode(), response.getBody().getCode());
        
        Map<String, String> errors = response.getBody().getData();
        assertNotNull(errors);
        assertEquals(2, errors.size());
        assertEquals("Email is required", errors.get("email"));
        assertEquals("Name must not be blank", errors.get("name"));
    }

    @Test
    void handleIllegalArgumentException_ShouldReturn400() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument provided");
        
        // When
        ResponseEntity<ApiResponse<Object>> response = 
                exceptionHandler.handleIllegalArgumentException(ex);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorCode.VAL_400_ILLEGAL_ARGUMENT.getCode(), response.getBody().getCode());
    }

    @Test
    void handleGenericException_ShouldReturn500() {
        // Given
        Exception ex = new RuntimeException("Unexpected error");
        
        // When
        ResponseEntity<ApiResponse<Object>> response = 
                exceptionHandler.handleGenericException(ex);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorCode.SYS_500_INTERNAL_ERROR.getCode(), response.getBody().getCode());
    }

    @Test
    void handleBaseException_ShouldMapToCorrectHttpStatus() {
        // Given - ResourceNotFoundException extends BaseException
        // Note: In actual Spring exception handling, ResourceNotFoundException would be handled
        // by the more specific handler. This test verifies the BaseException handler fallback.
        ResourceNotFoundException ex = new ResourceNotFoundException("User", "id", 1L);
        
        // When
        ResponseEntity<ApiResponse<Object>> response = 
                exceptionHandler.handleBaseException(ex);
        
        // Then - BaseException handler always returns 500
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void handleBaseException_WithBusinessException_ShouldMapTo500() {
        // Given
        // Note: In actual Spring exception handling, BusinessException would be handled
        // by the more specific handler. This test verifies the BaseException handler fallback.
        BusinessException ex = new BusinessException(
                ErrorCode.BUSINESS_400_ERROR,
                "Business rule violation"
        );
        
        // When
        ResponseEntity<ApiResponse<Object>> response = 
                exceptionHandler.handleBaseException(ex);
        
        // Then - BaseException handler always returns 500
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void handleBaseException_WithSystemException_ShouldMapTo500() {
        // Given
        SystemException ex = new SystemException(
                ErrorCode.SYS_500_INTERNAL_ERROR,
                "System error"
        );
        
        // When
        ResponseEntity<ApiResponse<Object>> response = 
                exceptionHandler.handleBaseException(ex);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}

