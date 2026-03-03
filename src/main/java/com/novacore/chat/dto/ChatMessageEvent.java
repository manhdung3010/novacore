package com.novacore.chat.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageEvent {

    private String eventType;
    private Long serverId;
    private Long channelId;
    private Long authorId;
    private String content;
    private Instant createdAt;
}

