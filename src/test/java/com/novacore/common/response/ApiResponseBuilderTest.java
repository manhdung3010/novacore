package com.novacore.common.response;

import com.novacore.shared.constants.ErrorCode;
import com.novacore.shared.context.RequestContext;
import com.novacore.shared.context.RequestContextHolder;
import com.novacore.shared.response.ApiResponse;
import com.novacore.shared.response.ApiResponseBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ApiResponseBuilder
 * 
 * Tests response creation logic and RequestContext integration
 */
class ApiResponseBuilderTest {

    @BeforeEach
    void setUp() {
        // Clear any existing context
        RequestContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        // Clean up context after each test
        RequestContextHolder.clear();
    }

    @Test
    void success_WithData_ShouldCreateSuccessResponse() {
        // Given - Use Integer to avoid method overload ambiguity with String
        Integer testData = 42;
        
        // When
        ApiResponse<Integer> response = ApiResponseBuilder.success(testData);
        
        // Then
        assertTrue(response.isSuccess());
        assertEquals(ErrorCode.SUCCESS_200_OK.getCode(), response.getCode());
        assertEquals(ErrorCode.SUCCESS_200_OK.getDefaultMessage(), response.getMessage());
        assertEquals(testData, response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void success_WithMessageAndData_ShouldCreateSuccessResponse() {
        // Given
        String message = "Custom success message";
        String data = "test data";
        
        // When
        ApiResponse<String> response = ApiResponseBuilder.success(message, data);
        
        // Then
        assertTrue(response.isSuccess());
        assertEquals(ErrorCode.SUCCESS_200_OK.getCode(), response.getCode());
        assertEquals(message, response.getMessage());
        assertEquals(data, response.getData());
    }

    @Test
    void success_WithMessageOnly_ShouldCreateSuccessResponseWithoutData() {
        // Given
        String message = "Operation completed";
        
        // When
        ApiResponse<Void> response = ApiResponseBuilder.success(message);
        
        // Then
        assertTrue(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void success_WithRequestContext_ShouldIncludeRequestIdAndPath() {
        // Given
        RequestContext context = RequestContext.builder()
                .requestId("test-request-id")
                .traceId("test-trace-id")
                .path("/api/v1/users")
                .requestStartTime(System.currentTimeMillis())
                .build();
        RequestContextHolder.set(context);
        
        // When
        ApiResponse<String> response = ApiResponseBuilder.success("test");
        
        // Then
        assertEquals("test-request-id", response.getRequestId());
        assertEquals("/api/v1/users", response.getPath());
    }

    @Test
    void success_WithoutRequestContext_ShouldReturnNullRequestId() {
        // Given - no context set
        
        // When
        ApiResponse<String> response = ApiResponseBuilder.success("test");
        
        // Then
        assertNull(response.getRequestId()); // No context, no requestId
        assertNull(response.getPath()); // No context, no path
    }

    @Test
    void error_WithErrorCode_ShouldCreateErrorResponse() {
        // When
        ApiResponse<Object> response = ApiResponseBuilder.error(ErrorCode.RESOURCE_404_NOT_FOUND);
        
        // Then
        assertFalse(response.isSuccess());
        assertEquals(ErrorCode.RESOURCE_404_NOT_FOUND.getCode(), response.getCode());
        assertEquals(ErrorCode.RESOURCE_404_NOT_FOUND.getDefaultMessage(), response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void error_WithErrorCodeAndMessage_ShouldCreateErrorResponse() {
        // Given
        String customMessage = "Resource not found with custom message";
        
        // When
        ApiResponse<Object> response = ApiResponseBuilder.error(
                ErrorCode.RESOURCE_404_NOT_FOUND,
                customMessage
        );
        
        // Then
        assertFalse(response.isSuccess());
        assertEquals(ErrorCode.RESOURCE_404_NOT_FOUND.getCode(), response.getCode());
        assertEquals(customMessage, response.getMessage());
    }

    @Test
    void error_WithErrorCodeMessageAndDetails_ShouldCreateErrorResponse() {
        // Given
        String message = "Validation failed";
        String errorDetails = "Email format invalid";
        
        // When
        ApiResponse<Object> response = ApiResponseBuilder.error(
                ErrorCode.VAL_400_VALIDATION_ERROR,
                message,
                errorDetails
        );
        
        // Then
        assertFalse(response.isSuccess());
        assertEquals(ErrorCode.VAL_400_VALIDATION_ERROR.getCode(), response.getCode());
        assertEquals(message, response.getMessage());
        assertEquals(errorDetails, response.getError());
    }

    @Test
    void error_WithRequestContext_ShouldIncludeRequestIdAndPath() {
        // Given
        RequestContext context = RequestContext.builder()
                .requestId("error-request-id")
                .traceId("error-trace-id")
                .path("/api/v1/resources/123")
                .requestStartTime(System.currentTimeMillis())
                .build();
        RequestContextHolder.set(context);
        
        // When
        ApiResponse<Object> response = ApiResponseBuilder.error(ErrorCode.RESOURCE_404_NOT_FOUND);
        
        // Then
        assertEquals("error-request-id", response.getRequestId());
        assertEquals("/api/v1/resources/123", response.getPath());
    }

    @Test
    void error_WithBusinessErrorCode_ShouldCreateCorrectResponse() {
        // When
        ApiResponse<Object> response = ApiResponseBuilder.error(ErrorCode.BUSINESS_400_ERROR);
        
        // Then
        assertFalse(response.isSuccess());
        assertEquals("BUSINESS_400_ERROR", response.getCode());
        assertEquals("Business rule violation", response.getMessage());
    }

    @Test
    void error_WithSystemErrorCode_ShouldCreateCorrectResponse() {
        // When
        ApiResponse<Object> response = ApiResponseBuilder.error(ErrorCode.SYS_500_INTERNAL_ERROR);
        
        // Then
        assertFalse(response.isSuccess());
        assertEquals("SYS_500_INTERNAL_ERROR", response.getCode());
        assertEquals("Internal server error", response.getMessage());
    }
}

