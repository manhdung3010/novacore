package com.novacore.chat.controller;

import com.novacore.chat.dto.ChatWsSendMessage;
import com.novacore.chat.dto.SendMessageRequest;
import com.novacore.chat.service.ChatRealtimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatRealtimeService chatRealtimeService;

    /**
     * WebSocket STOMP entrypoint for sending chat messages.
     * Destination: /app/chat.sendMessage
     *
     * Clients should send payload:
     * { "serverId": 1, "channelId": 10, "content": "Hello" }
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(
            @Valid ChatWsSendMessage payload,
            @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {
        Long authorId = (Long) sessionAttributes.get("userId");
        if (authorId == null) {
            // Fallback: controller-level guard, tránh vào service với user null
            throw new com.novacore.shared.exception.BusinessException(
                    com.novacore.shared.constants.ErrorCode.AUTH_401_UNAUTHORIZED,
                    "User is not authenticated for WebSocket message");
        }
        SendMessageRequest request = new SendMessageRequest(payload.getContent());
        chatRealtimeService.sendMessage(payload.getServerId(), payload.getChannelId(), authorId, request);
    }
}

