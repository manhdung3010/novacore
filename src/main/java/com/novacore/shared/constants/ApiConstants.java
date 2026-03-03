package com.novacore.shared.constants;

public final class ApiConstants {
    
    private ApiConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String API_BASE_PATH = "/api";
    
    public static final String DEFAULT_API_VERSION = "v1";
    
    public static final String HEALTH_ENDPOINT = "/health";

    public static final String AUTH_ENDPOINT = "/api/v1/auth";
    public static final String USERS_ENDPOINT = "/api/v1/users";
    public static final String SERVERS_ENDPOINT = "/api/v1/servers";
    public static final String INVITES_ENDPOINT = "/api/v1/invites";
    
    public static final String API_V1_BASE = "/api/v1";
    
    public static String buildApiPath(String version, String endpoint) {
        if (version == null || version.isBlank()) {
            version = DEFAULT_API_VERSION;
        }
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException("Endpoint cannot be null or blank");
        }
        String normalizedEndpoint = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
        return API_BASE_PATH + "/" + version + normalizedEndpoint;
    }
    
    public static String buildApiPath(String endpoint) {
        return buildApiPath(DEFAULT_API_VERSION, endpoint);
    }
    
    @Deprecated(since = "1.0.0", forRemoval = true)
    public static final String API_VERSION = DEFAULT_API_VERSION;
    
    @Deprecated(since = "1.0.0", forRemoval = true)
    public static final String API_V1 = buildApiPath(DEFAULT_API_VERSION, "");
}

