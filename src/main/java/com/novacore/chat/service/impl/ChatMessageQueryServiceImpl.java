package com.novacore.chat.service.impl;

import com.novacore.chat.domain.ChatMessage;
import com.novacore.chat.dto.ChatMessageDto;
import com.novacore.chat.repository.ChatMessageRepository;
import com.novacore.chat.service.ChatMessageQueryService;
import com.novacore.server.domain.ServerChannel;
import com.novacore.server.repository.ServerChannelRepository;
import com.novacore.server.repository.ServerMemberRepository;
import com.novacore.shared.constants.ErrorCode;
import com.novacore.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageQueryServiceImpl implements ChatMessageQueryService {

    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 100;

    private final ChatMessageRepository chatMessageRepository;
    private final ServerChannelRepository serverChannelRepository;
    private final ServerMemberRepository serverMemberRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDto> listChannelMessages(
            Long serverId,
            Long channelId,
            Long requesterId,
            Instant before,
            int limit) {

        if (serverId == null || channelId == null || requesterId == null) {
            throw new BusinessException(ErrorCode.BUSINESS_400_ERROR, "Missing required parameters");
        }
        if (limit < MIN_LIMIT || limit > MAX_LIMIT) {
            throw new BusinessException(ErrorCode.BUSINESS_400_ERROR, "Limit must be between " + MIN_LIMIT + " and " + MAX_LIMIT);
        }

        ServerChannel channel = serverChannelRepository.findById(channelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_404_NOT_FOUND, "Channel not found: " + channelId));
        if (channel.getServer() == null || !channel.getServer().getId().equals(serverId)) {
            throw new BusinessException(ErrorCode.BUSINESS_400_ERROR, "Channel does not belong to server " + serverId);
        }

        boolean isOwner = channel.getServer() != null
                && channel.getServer().getOwner() != null
                && channel.getServer().getOwner().getId().equals(requesterId);
        boolean isMember = isOwner || serverMemberRepository.existsByServerIdAndUserId(serverId, requesterId);
        if (!isMember) {
            throw new BusinessException(ErrorCode.AUTH_403_FORBIDDEN, "You are not a member of this server");
        }

        PageRequest pageRequest = PageRequest.of(0, limit);
        Slice<ChatMessage> slice = (before == null)
                ? chatMessageRepository.findByChannel_IdOrderByCreatedAtDesc(channelId, pageRequest)
                : chatMessageRepository.findByChannel_IdAndCreatedAtLessThanOrderByCreatedAtDesc(channelId, before, pageRequest);

        return slice.getContent().stream().map(this::toDto).toList();
    }

    private ChatMessageDto toDto(ChatMessage message) {
        return ChatMessageDto.builder()
                .id(message.getId())
                .messageId(message.getMessageUuid() == null ? null : message.getMessageUuid().toString())
                .serverId(message.getChannel() == null || message.getChannel().getServer() == null ? null : message.getChannel().getServer().getId())
                .channelId(message.getChannel() == null ? null : message.getChannel().getId())
                .authorId(message.getAuthor() == null ? null : message.getAuthor().getId())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .editedAt(message.getEditedAt())
                .build();
    }
}

