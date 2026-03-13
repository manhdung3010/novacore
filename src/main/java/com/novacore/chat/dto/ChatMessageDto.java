package com.novacore.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private Long id;
    private String messageId;
    private Long serverId;
    private Long channelId;
    private Long authorId;
    private String content;
    private Instant createdAt;
    private Instant editedAt;
}

