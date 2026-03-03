package com.novacore.chat.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacore.chat.dto.ChatMessageEvent;
import com.novacore.chat.dto.SendMessageRequest;
import com.novacore.chat.service.ChatRealtimeService;
import com.novacore.server.domain.ChannelType;
import com.novacore.server.domain.ServerChannel;
import com.novacore.server.repository.ServerChannelRepository;
import com.novacore.server.repository.ServerMemberRepository;
import com.novacore.shared.constants.ErrorCode;
import com.novacore.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ChatRealtimeServiceImpl implements ChatRealtimeService {

    private static final String TOPIC_CHAT_MESSAGES = "chat-messages";

    private final ServerChannelRepository serverChannelRepository;
    private final ServerMemberRepository serverMemberRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public ChatMessageEvent sendMessage(Long serverId, Long channelId, Long authorId, SendMessageRequest request) {
        ServerChannel channel = serverChannelRepository.findById(channelId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_404_NOT_FOUND,
                        "Channel not found: " + channelId));

        if (channel.getServer() == null || !channel.getServer().getId().equals(serverId)) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_400_ERROR,
                    "Channel does not belong to server " + serverId);
        }
        if (channel.getType() != ChannelType.text) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_400_ERROR,
                    "Cannot send text message to non-text channel");
        }

        boolean isOwner = channel.getServer() != null
                && channel.getServer().getOwner() != null
                && channel.getServer().getOwner().getId().equals(authorId);

        boolean isMember = isOwner || serverMemberRepository.existsByServerIdAndUserId(serverId, authorId);
        if (!isMember) {
            throw new BusinessException(
                    ErrorCode.AUTH_403_FORBIDDEN,
                    "You are not a member of this server");
        }

        ChatMessageEvent event = ChatMessageEvent.builder()
                .eventType("MESSAGE_CREATED")
                .serverId(serverId)
                .channelId(channelId)
                .authorId(authorId)
                .content(request.getContent())
                .createdAt(Instant.now())
                .build();

        String key = serverId + ":" + channelId;
        String payload = toJson(event);

        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_CHAT_MESSAGES, key, payload);
        kafkaTemplate.send(record);

        return event;
    }

    private String toJson(ChatMessageEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new BusinessException(
                    ErrorCode.SYS_500_INTERNAL_ERROR,
                    "Failed to serialize chat message event");
        }
    }
}

