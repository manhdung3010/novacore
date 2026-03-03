package com.novacore.chat.service;

import com.novacore.chat.dto.ChatMessageEvent;
import com.novacore.chat.dto.SendMessageRequest;

public interface ChatRealtimeService {

    ChatMessageEvent sendMessage(Long serverId, Long channelId, Long authorId, SendMessageRequest request);
}

