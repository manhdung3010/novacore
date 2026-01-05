package com.novacore.shared.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacore.shared.constants.Channel;
import com.novacore.shared.constants.ErrorCode;
import com.novacore.shared.constants.MdcConstants;
import com.novacore.shared.constants.RequestHeaderConstants;
import com.novacore.shared.response.ApiResponse;
import com.novacore.shared.response.ApiResponseBuilder;
import com.novacore.shared.util.LocaleUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component("customRequestContextFilter")
@Order(1)
public class RequestContextFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    public RequestContextFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String traceId = null;
        String requestId = null;
        
        try {
            traceId = extractTraceId(request);
            requestId = extractRequestId(request);
            
            MDC.put(MdcConstants.TRACE_ID, traceId);
            MDC.put(MdcConstants.REQUEST_ID, requestId);
            
            Long tenantId = extractTenantId(request);
            Channel channel = extractChannel(request);
            String clientIp = getClientIp(request);
            
            RequestContext context = RequestContext.builder()
                    .requestId(requestId)
                    .traceId(traceId)
                    .path(request.getRequestURI())
                    .method(request.getMethod())
                    .clientIp(clientIp)
                    .locale(LocaleUtils.resolveLocale(request))
                    .tenantId(tenantId)
                    .channel(channel)
                    .requestStartTime(System.currentTimeMillis())
                    .build();
            
            RequestContextHolder.set(context);
            
            response.setHeader(RequestHeaderConstants.TRACE_ID_HEADER, traceId);
            response.setHeader(RequestHeaderConstants.REQUEST_ID_HEADER, requestId);
            
            filterChain.doFilter(request, response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request context [traceId={}, requestId={}]: {}", traceId, requestId, e.getMessage());
            handleFilterException(response, e, HttpStatus.BAD_REQUEST, ErrorCode.VAL_400_ILLEGAL_ARGUMENT);
        } catch (Exception e) {
            log.error("Unexpected error in RequestContextFilter [traceId={}, requestId={}]", traceId, requestId, e);
            handleFilterException(response, e, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.SYS_500_INTERNAL_ERROR);
        } finally {
            RequestContextHolder.clear();
            MDC.clear();
        }
    }

    private String extractTraceId(HttpServletRequest request) {
        String traceId = request.getHeader(RequestHeaderConstants.TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        return traceId;
    }

    private String extractRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(RequestHeaderConstants.REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        return requestId;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader(RequestHeaderConstants.X_FORWARDED_FOR_HEADER);
        if (isValidIp(ip)) {
            if (ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        }
        
        ip = request.getHeader(RequestHeaderConstants.X_REAL_IP_HEADER);
        if (isValidIp(ip)) {
            return ip;
        }
        
        return request.getRemoteAddr();
    }

    private boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }

    private Long extractTenantId(HttpServletRequest request) {
        String tenantIdHeader = request.getHeader(RequestHeaderConstants.TENANT_ID_HEADER);
        if (tenantIdHeader == null || tenantIdHeader.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(tenantIdHeader.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("Invalid tenant ID format in header '%s': expected Long, got '%s'", 
                            RequestHeaderConstants.TENANT_ID_HEADER, tenantIdHeader));
        }
    }

    private Channel extractChannel(HttpServletRequest request) {
        String channelHeader = request.getHeader(RequestHeaderConstants.CHANNEL_HEADER);
        return Channel.from(channelHeader);
    }

    private void handleFilterException(HttpServletResponse response, Exception ex, 
                                       HttpStatus status, ErrorCode errorCode) {
        String message = errorCode.getDefaultMessage();
        
        try {
            response.setStatus(status.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            ApiResponse<Object> errorResponse = ApiResponseBuilder.error(errorCode, message);
            objectMapper.writeValue(response.getWriter(), errorResponse);
        } catch (IOException ioException) {
            log.error("Failed to write error response to client", ioException);
            try {
                response.sendError(status.value(), message);
            } catch (IOException e) {
                log.error("Failed to send error response", e);
            }
        }
    }
}





