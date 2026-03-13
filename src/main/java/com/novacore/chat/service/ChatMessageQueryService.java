package com.novacore.chat.service;

import com.novacore.chat.dto.ChatMessageDto;

import java.time.Instant;
import java.util.List;

public interface ChatMessageQueryService {

    List<ChatMessageDto> listChannelMessages(Long serverId, Long channelId, Long requesterId, Instant before, int limit);
}

