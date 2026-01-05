package com.novacore.common.context;

import com.novacore.shared.constants.Channel;
import com.novacore.shared.context.RequestContext;
import com.novacore.shared.context.RequestContextHolder;
import com.novacore.shared.context.RequestContextNotAvailableException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RequestContextHolder.
 * 
 * Tests ThreadLocal management and utility methods.
 */
class RequestContextHolderTest {

    @BeforeEach
    void setUp() {
        RequestContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Test
    void get_WithNoContext_ShouldReturnNull() {
        // When
        RequestContext context = RequestContextHolder.get();
        
        // Then
        assertNull(context);
    }

    @Test
    void get_WithContext_ShouldReturnContext() {
        // Given
        RequestContext expectedContext = RequestContext.builder()
                .requestId("test-id")
                .traceId("trace-id")
                .requestStartTime(System.currentTimeMillis())
                .build();
        RequestContextHolder.set(expectedContext);
        
        // When
        RequestContext actualContext = RequestContextHolder.get();
        
        // Then
        assertNotNull(actualContext);
        assertEquals("test-id", actualContext.getRequestId());
        assertEquals("trace-id", actualContext.getTraceId());
    }

    @Test
    void require_WithNoContext_ShouldThrowException() {
        // When/Then
        RequestContextNotAvailableException exception = assertThrows(RequestContextNotAvailableException.class, () -> {
            RequestContextHolder.require();
        });
        
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("RequestContext not available"));
    }

    @Test
    void require_WithContext_ShouldReturnContext() {
        // Given
        RequestContext expectedContext = RequestContext.builder()
                .requestId("test-id")
                .traceId("trace-id")
                .requestStartTime(System.currentTimeMillis())
                .build();
        RequestContextHolder.set(expectedContext);
        
        // When
        RequestContext actualContext = RequestContextHolder.require();
        
        // Then
        assertNotNull(actualContext);
        assertEquals("test-id", actualContext.getRequestId());
    }

    @Test
    void set_WithNull_ShouldClearContext() {
        // Given
        RequestContext context = RequestContext.builder()
                .requestId("test-id")
                .traceId("trace-id")
                .requestStartTime(System.currentTimeMillis())
                .build();
        RequestContextHolder.set(context);
        assertTrue(RequestContextHolder.hasContext());
        
        // When
        RequestContextHolder.set(null);
        
        // Then
        assertFalse(RequestContextHolder.hasContext());
    }

    @Test
    void clear_ShouldRemoveContext() {
        // Given
        RequestContext context = RequestContext.builder()
                .requestId("test-id")
                .traceId("trace-id")
                .requestStartTime(System.currentTimeMillis())
                .build();
        RequestContextHolder.set(context);
        assertTrue(RequestContextHolder.hasContext());
        
        // When
        RequestContextHolder.clear();
        
        // Then
        assertFalse(RequestContextHolder.hasContext());
        assertNull(RequestContextHolder.get());
    }

    @Test
    void hasContext_WithNoContext_ShouldReturnFalse() {
        // When
        boolean hasContext = RequestContextHolder.hasContext();
        
        // Then
        assertFalse(hasContext);
    }

    @Test
    void hasContext_WithContext_ShouldReturnTrue() {
        // Given
        RequestContext context = RequestContext.builder()
                .requestId("test-id")
                .traceId("trace-id")
                .requestStartTime(System.currentTimeMillis())
                .build();
        RequestContextHolder.set(context);
        
        // When
        boolean hasContext = RequestContextHolder.hasContext();
        
        // Then
        assertTrue(hasContext);
    }

    @Test
    void updateUser_WithNoContext_ShouldThrowException() {
        // When/Then
        assertThrows(RequestContextNotAvailableException.class, () -> {
            RequestContextHolder.updateUser(1L, "user@example.com");
        });
    }

    @Test
    void updateUser_WithContext_ShouldUpdateUserInfo() {
        // Given
        long startTime = System.currentTimeMillis();
        RequestContext originalContext = RequestContext.builder()
                .requestId("req-123")
                .traceId("trace-456")
                .path("/api/users")
                .method("GET")
                .clientIp("127.0.0.1")
                .locale(Locale.US)
                .tenantId(100L)
                .channel(Channel.WEB)
                .requestStartTime(startTime)
                .build();
        RequestContextHolder.set(originalContext);
        
        // When
        RequestContextHolder.updateUser(999L, "updated@example.com");
        
        // Then
        RequestContext updatedContext = RequestContextHolder.require();
        assertEquals(999L, updatedContext.getCurrentUserId());
        assertEquals("updated@example.com", updatedContext.getCurrentUserEmail());
        
        // Verify other fields are preserved (including requestStartTime via toBuilder())
        assertEquals("req-123", updatedContext.getRequestId());
        assertEquals("trace-456", updatedContext.getTraceId());
        assertEquals("/api/users", updatedContext.getPath());
        assertEquals("GET", updatedContext.getMethod());
        assertEquals("127.0.0.1", updatedContext.getClientIp());
        assertEquals(Locale.US, updatedContext.getLocale());
        assertEquals(100L, updatedContext.getTenantId());
        assertEquals(Channel.WEB, updatedContext.getChannel());
        assertEquals(startTime, updatedContext.getRequestStartTime());
    }

    @Test
    void getRequestId_WithContext_ShouldReturnRequestId() {
        // Given
        RequestContext context = RequestContext.builder()
                .requestId("request-123")
                .traceId("trace-123")
                .requestStartTime(System.currentTimeMillis())
                .build();
        RequestContextHolder.set(context);
        
        // When
        String requestId = RequestContextHolder.getRequestId();
        
        // Then
        assertEquals("request-123", requestId);
    }

    @Test
    void getRequestId_WithNoContext_ShouldReturnNull() {
        // When
        String requestId = RequestContextHolder.getRequestId();
        
        // Then
        assertNull(requestId);
    }

    @Test
    void getTraceId_WithContext_ShouldReturnTraceId() {
        // Given
        RequestContext context = RequestContext.builder()
                .traceId("trace-789")
                .requestId("request-789")
                .requestStartTime(System.currentTimeMillis())
                .build();
        RequestContextHolder.set(context);
        
        // When
        String traceId = RequestContextHolder.getTraceId();
        
        // Then
        assertEquals("trace-789", traceId);
    }

    @Test
    void getTraceId_WithNoContext_ShouldReturnNull() {
        // When
        String traceId = RequestContextHolder.getTraceId();
        
        // Then
        assertNull(traceId);
    }

    @Test
    void getCurrentUserId_WithContext_ShouldReturnUserId() {
        // Given
        RequestContext context = RequestContext.builder()
                .traceId("trace-id")
                .requestId("test-id")
                .requestStartTime(System.currentTimeMillis())
                .currentUserId(42L)
                .currentUserEmail("user@example.com")
                .build();
        RequestContextHolder.set(context);
        
        // When
        Long userId = RequestContextHolder.getCurrentUserId();
        
        // Then
        assertEquals(42L, userId);
    }

    @Test
    void getCurrentUserId_WithNoContext_ShouldReturnNull() {
        // When
        Long userId = RequestContextHolder.getCurrentUserId();
        
        // Then
        assertNull(userId);
    }
}


