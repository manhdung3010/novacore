package com.novacore.chat.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacore.chat.domain.ChatMessage;
import com.novacore.chat.dto.ChatMessageEvent;
import com.novacore.chat.repository.ChatMessageRepository;
import com.novacore.server.domain.ServerChannel;
import com.novacore.server.repository.ServerChannelRepository;
import com.novacore.user.domain.User;
import com.novacore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMessageKafkaListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final ServerChannelRepository serverChannelRepository;
    private final UserRepository userRepository;

    @KafkaListener(topics = "chat-messages", groupId = "novacore-chat-realtime")
    public void onMessage(String payload) {
        ChatMessageEvent event = fromJson(payload);
        if (event == null) {
            return;
        }

        persistMessage(event);

        String destination = String.format(
                "/topic/servers/%d/channels/%d",
                event.getServerId(),
                event.getChannelId());

        messagingTemplate.convertAndSend(destination, event);
    }

    private void persistMessage(ChatMessageEvent event) {
        if (event.getMessageId() == null || event.getMessageId().isBlank()) {
            log.warn("ChatMessageEvent missing messageId; skipping persistence");
            return;
        }

        UUID messageUuid;
        try {
            messageUuid = UUID.fromString(event.getMessageId());
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid messageId UUID {}; skipping persistence", event.getMessageId());
            return;
        }

        if (chatMessageRepository.existsByMessageUuid(messageUuid)) {
            return;
        }

        Optional<ServerChannel> channelOpt = serverChannelRepository.findById(event.getChannelId());
        if (channelOpt.isEmpty()) {
            log.warn("Channel {} not found; skipping persistence", event.getChannelId());
            return;
        }

        Optional<User> authorOpt = event.getAuthorId() == null
                ? Optional.empty()
                : userRepository.findById(event.getAuthorId());

        ChatMessage message = ChatMessage.builder()
                .messageUuid(messageUuid)
                .channel(channelOpt.get())
                .author(authorOpt.orElse(null))
                .content(event.getContent())
                .createdAt(event.getCreatedAt())
                .editedAt(null)
                .build();

        chatMessageRepository.save(message);
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

