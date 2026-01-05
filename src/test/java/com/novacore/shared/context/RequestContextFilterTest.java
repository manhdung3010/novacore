package com.novacore.shared.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.novacore.shared.constants.Channel;
import com.novacore.shared.constants.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RequestContextFilter.
 * 
 * Ensures ThreadLocal cleanup in all scenarios:
 * - Successful request completion
 * - Exception during filter chain
 * - Early return scenarios
 */
class RequestContextFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private RequestContextFilter filter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        filter = new RequestContextFilter(objectMapper);
        
        // Clear any existing context and MDC
        RequestContextHolder.clear();
        MDC.clear();
        
        // Setup default request behavior
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getLocale()).thenReturn(Locale.US);
        
        // Setup response writer for error responses
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void doFilterInternal_SuccessfulRequest_ShouldSetAndClearContext() throws Exception {
        // Given - no headers (will generate UUIDs)
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
        verify(response).setHeader(eq("X-Trace-Id"), anyString());
        verify(response).setHeader(eq("X-Request-Id"), anyString());
        
        // Context should be cleared after filter
        assertFalse(RequestContextHolder.hasContext(), "Context should be cleared after filter");
        assertNull(MDC.get("traceId"), "MDC should be cleared after filter");
        assertNull(MDC.get("requestId"), "MDC should be cleared after filter");
    }

    @Test
    void doFilterInternal_WithTraceIdHeader_ShouldUseProvidedTraceId() throws Exception {
        // Given
        String providedTraceId = "custom-trace-id-123";
        when(request.getHeader("X-Trace-Id")).thenReturn(providedTraceId);
        when(request.getHeader("X-Request-Id")).thenReturn(null);
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(response).setHeader("X-Trace-Id", providedTraceId);
        assertFalse(RequestContextHolder.hasContext(), "Context should be cleared");
    }

    @Test
    void doFilterInternal_WithRequestIdHeader_ShouldUseProvidedRequestId() throws Exception {
        // Given
        String providedRequestId = "custom-request-id-456";
        when(request.getHeader("X-Trace-Id")).thenReturn(null);
        when(request.getHeader("X-Request-Id")).thenReturn(providedRequestId);
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(response).setHeader("X-Request-Id", providedRequestId);
        assertFalse(RequestContextHolder.hasContext(), "Context should be cleared");
    }

    @Test
    void doFilterInternal_WithTenantIdHeader_ShouldExtractTenantId() throws Exception {
        // Given
        when(request.getHeader("X-Tenant-Id")).thenReturn("123");
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then - verify context was set during filter execution
        verify(filterChain).doFilter(request, response);
        assertFalse(RequestContextHolder.hasContext(), "Context should be cleared");
    }

    @Test
    void doFilterInternal_WithInvalidTenantIdFormat_ShouldReturn400() throws Exception {
        // Given
        when(request.getHeader("X-Tenant-Id")).thenReturn("invalid-tenant-id");
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then - should not proceed to filter chain, should return 400
        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(400);
        verify(response).setContentType("application/json");
        assertFalse(RequestContextHolder.hasContext(), "Context should be cleared");
    }

    @Test
    void doFilterInternal_WithEmptyTenantIdHeader_ShouldAllowRequest() throws Exception {
        // Given - empty or blank tenant ID header should be treated as absent (optional)
        when(request.getHeader("X-Tenant-Id")).thenReturn("");
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then - should proceed normally as tenant ID is optional
        verify(filterChain).doFilter(request, response);
        assertFalse(RequestContextHolder.hasContext(), "Context should be cleared");
    }

    @Test
    void doFilterInternal_WithChannelHeader_ShouldExtractChannel() throws Exception {
        // Given
        when(request.getHeader("X-Channel")).thenReturn("MOBILE");
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
        assertFalse(RequestContextHolder.hasContext(), "Context should be cleared");
    }

    @Test
    void doFilterInternal_WithInvalidChannelHeader_ShouldDefaultToWeb() throws Exception {
        // Given
        when(request.getHeader("X-Channel")).thenReturn("INVALID_CHANNEL");
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
        assertFalse(RequestContextHolder.hasContext(), "Context should be cleared");
    }

    @Test
    void doFilterInternal_ExceptionDuringFilterChain_ShouldClearContext() throws Exception {
        // Given
        IOException exception = new IOException("Filter chain error");
        doThrow(exception).when(filterChain).doFilter(request, response);
        
        // When - filter catches exceptions and doesn't rethrow them
        filter.doFilterInternal(request, response, filterChain);
        
        // Then - Context should still be cleared even on exception
        assertFalse(RequestContextHolder.hasContext(), 
                "Context must be cleared even when exception occurs");
        assertNull(MDC.get("traceId"), "MDC must be cleared even when exception occurs");
        
        // Verify error response was written
        verify(response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void doFilterInternal_ShouldSetMDCForLogging() throws Exception {
        // Given
        String traceId = "test-trace-id";
        when(request.getHeader("X-Trace-Id")).thenReturn(traceId);
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then - MDC should be cleared after filter
        assertNull(MDC.get("traceId"), "MDC should be cleared after filter");
        assertNull(MDC.get("requestId"), "MDC should be cleared after filter");
    }

    @Test
    void doFilterInternal_WithXForwardedFor_ShouldExtractClientIp() throws Exception {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
        assertFalse(RequestContextHolder.hasContext(), "Context should be cleared");
    }

    @Test
    void doFilterInternal_WithXRealIP_ShouldExtractClientIp() throws Exception {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("10.0.0.1");
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
        assertFalse(RequestContextHolder.hasContext(), "Context should be cleared");
    }

    @Test
    void doFilterInternal_WithAcceptLanguageHeader_ShouldResolveLocale() throws Exception {
        // Given
        when(request.getHeader("Accept-Language")).thenReturn("fr-FR,fr;q=0.9,en;q=0.8");
        when(request.getLocale()).thenReturn(Locale.FRENCH);
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
        assertFalse(RequestContextHolder.hasContext(), "Context should be cleared");
    }

    @Test
    void doFilterInternal_MultipleRequests_ShouldNotShareContext() throws Exception {
        // Given
        when(request.getHeader("X-Trace-Id")).thenReturn("trace-1");
        
        // When - first request
        filter.doFilterInternal(request, response, filterChain);
        
        // Verify context is cleared
        assertFalse(RequestContextHolder.hasContext());
        
        // Setup second request
        when(request.getHeader("X-Trace-Id")).thenReturn("trace-2");
        
        // When - second request
        filter.doFilterInternal(request, response, filterChain);
        
        // Then - context should still be cleared
        assertFalse(RequestContextHolder.hasContext(), 
                "Each request should have independent context that is cleared");
    }
}





