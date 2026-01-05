package com.novacore.shared.constants;

public final class RequestHeaderConstants {
    
    private RequestHeaderConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String TENANT_ID_HEADER = "X-Tenant-Id";
    public static final String CHANNEL_HEADER = "X-Channel";
    public static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";
    public static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";
    public static final String X_REAL_IP_HEADER = "X-Real-IP";
}





