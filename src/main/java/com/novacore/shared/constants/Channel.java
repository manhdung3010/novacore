package com.novacore.shared.constants;

public enum Channel {
    WEB,
    MOBILE,
    INTERNAL;
    
    public static Channel from(String value) {
        if (value == null || value.isBlank()) {
            return WEB;
        }
        
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return WEB;
        }
    }
    
    public static Channel from(String value, Channel defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }
}



