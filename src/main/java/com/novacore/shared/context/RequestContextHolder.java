package com.novacore.shared.context;

import com.novacore.shared.constants.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NamedThreadLocal;

import java.util.Locale;
import java.util.function.Function;

@Slf4j
public final class RequestContextHolder {

    private static final ThreadLocal<RequestContext> CONTEXT_HOLDER = 
            new NamedThreadLocal<>("request-context");

    private RequestContextHolder() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static RequestContext get() {
        return CONTEXT_HOLDER.get();
    }

    public static RequestContext require() {
        RequestContext ctx = get();
        if (ctx == null) {
            throw new RequestContextNotAvailableException();
        }
        return ctx;
    }

    public static void set(RequestContext context) {
        if (context == null) {
            log.debug("Attempting to set null RequestContext - clearing instead");
            clear();
            return;
        }
        CONTEXT_HOLDER.set(context);
    }

    public static void clear() {
        CONTEXT_HOLDER.remove();
    }

    public static boolean hasContext() {
        return get() != null;
    }

    public static void updateUser(Long userId, String email) {
        RequestContext current = require();
        RequestContext updated = current.withUser(userId, email);
        set(updated);
    }

    private static <T> T getValue(Function<RequestContext, T> extractor) {
        RequestContext context = get();
        return context != null ? extractor.apply(context) : null;
    }
    
    public static String getRequestId() {
        return getValue(RequestContext::getRequestId);
    }

    public static String getTraceId() {
        return getValue(RequestContext::getTraceId);
    }

    public static Long getCurrentUserId() {
        return getValue(RequestContext::getCurrentUserId);
    }

    public static String getCurrentUserEmail() {
        return getValue(RequestContext::getCurrentUserEmail);
    }

    public static Long getTenantId() {
        return getValue(RequestContext::getTenantId);
    }

    public static Channel getChannel() {
        return getValue(RequestContext::getChannel);
    }

    public static Locale getLocale() {
        return getValue(RequestContext::getLocale);
    }

    public static String getPath() {
        return getValue(RequestContext::getPath);
    }

    public static String getMethod() {
        return getValue(RequestContext::getMethod);
    }

    public static String getClientIp() {
        return getValue(RequestContext::getClientIp);
    }
}





