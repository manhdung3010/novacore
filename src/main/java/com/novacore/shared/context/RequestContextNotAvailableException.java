package com.novacore.shared.context;

public class RequestContextNotAvailableException extends IllegalStateException {

    private static final String DEFAULT_MESSAGE = 
            "RequestContext not available in current thread. " +
            "Ensure RequestContextFilter is properly configured for web requests, " +
            "or use RequestContextTaskDecorator for async operations.";

    public RequestContextNotAvailableException() {
        super(DEFAULT_MESSAGE);
    }

    public RequestContextNotAvailableException(String message) {
        super(message);
    }

    public RequestContextNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}





