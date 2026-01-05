package com.novacore.shared.context;

import com.novacore.shared.constants.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Locale;
import java.util.Objects;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class RequestContext {
    
    private final Long currentUserId;
    private final String currentUserEmail;
    private final String traceId; 
    private final String requestId; 
    private final Locale locale;
    private final String path;
    private final String method;
    private final String clientIp;
    private final Long tenantId;
    
    @Builder.Default
    private final Channel channel = Channel.WEB;
    private final long requestStartTime;

    public long getElapsedTimeMs() {
        return System.currentTimeMillis() - requestStartTime;
    }

    public RequestContext withUser(Long userId, String email) {
        return this.toBuilder()
                .currentUserId(userId)
                .currentUserEmail(email)
                .build();
    }
}

