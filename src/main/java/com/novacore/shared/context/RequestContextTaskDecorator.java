package com.novacore.shared.context;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

@Slf4j
public class RequestContextTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        RequestContext contextSnapshot = RequestContextHolder.get();
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        
        if (contextSnapshot == null) {
            log.debug("No RequestContext available for async task - task will run without context");
        }
        
        return () -> {
            try {
                if (mdcContext != null && !mdcContext.isEmpty()) {
                    MDC.setContextMap(mdcContext);
                }
                
                if (contextSnapshot != null) {
                    RequestContextHolder.set(contextSnapshot);
                    log.trace("RequestContext propagated to async task [traceId={}, requestId={}]",
                            contextSnapshot.getTraceId(), contextSnapshot.getRequestId());
                }
                
                runnable.run();
            } catch (Exception e) {
                log.error("Error executing async task [traceId={}, requestId={}]",
                        contextSnapshot != null ? contextSnapshot.getTraceId() : "N/A",
                        contextSnapshot != null ? contextSnapshot.getRequestId() : "N/A", e);
            } finally {
                RequestContextHolder.clear();
                MDC.clear();
            }
        };
    }
}


















