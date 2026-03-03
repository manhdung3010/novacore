package com.novacore.chat.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacore.chat.dto.ChatMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMessageKafkaListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "chat-messages", groupId = "novacore-chat-realtime")
    public void onMessage(String payload) {
        ChatMessageEvent event = fromJson(payload);
        if (event == null) {
            return;
        }

        String destination = String.format(
                "/topic/servers/%d/channels/%d",
                event.getServerId(),
                event.getChannelId());

        messagingTemplate.convertAndSend(destination, event);
    }

    private ChatMessageEvent fromJson(String payload) {
        try {
            return objectMapper.readValue(payload, ChatMessageEvent.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize ChatMessageEvent from Kafka payload", e);
            return null;
        }
    }
}

