package com.novacore.server.service.impl;

import com.novacore.shared.constants.ErrorCode;
import com.novacore.shared.exception.BusinessException;
import com.novacore.shared.exception.ResourceNotFoundException;
import com.novacore.server.domain.Server;
import com.novacore.server.domain.ServerChannel;
import com.novacore.server.dto.CreateChannelRequest;
import com.novacore.server.dto.ServerChannelDto;
import com.novacore.server.dto.UpdateChannelRequest;
import com.novacore.server.repository.ServerChannelRepository;
import com.novacore.server.repository.ServerRepository;
import com.novacore.server.service.ServerChannelService;
import com.novacore.auth.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ServerChannelServiceImpl implements ServerChannelService {

    private final ServerChannelRepository channelRepository;
    private final ServerRepository serverRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public List<ServerChannelDto> getChannelsByServerId(Long serverId) {
        // Verify server exists
        if (!serverRepository.existsById(serverId)) {
            throw new ResourceNotFoundException("Server", "id", serverId);
        }

        return channelRepository.findByServerIdOrderByPosition(serverId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ServerChannelDto getChannel(Long serverId, Long channelId) {
        ServerChannel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel", "id", channelId));

        // Validate channel belongs to the specified server
        if (!channel.getServer().getId().equals(serverId)) {
            throw new BusinessException(ErrorCode.VAL_400_BAD_REQUEST, "Channel does not belong to this server");
        }

        return mapToDto(channel);
    }

    @Override
    public ServerChannelDto createChannel(Long serverId, CreateChannelRequest request) {
        // Verify server exists and current user is owner
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Server", "id", serverId));

        Long currentUserId = securityUtils.getCurrentUserId();
        if (!server.getOwner().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.AUTH_403_FORBIDDEN, "Only server owner can create channels");
        }

        // Validate parent channel if provided
        ServerChannel parentChannel = null;
        if (request.getParentId() != null) {
            parentChannel = channelRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Channel", "parentId", request.getParentId()));
            
            if (!parentChannel.getServer().getId().equals(serverId)) {
                throw new BusinessException(ErrorCode.VAL_400_BAD_REQUEST, "Parent channel must belong to the same server");
            }
        }

        ServerChannel channel = ServerChannel.builder()
                .server(server)
                .name(request.getName())
                .type(request.getType())
                .parent(parentChannel)
                .position(request.getPosition())
                .bitrate(request.getBitrate())
                .userLimit(request.getUserLimit())
                .nsfw(request.isNsfw())
                .build();

        ServerChannel savedChannel = channelRepository.save(channel);
        log.info("Created channel '{}' in server {}", savedChannel.getName(), serverId);

        return mapToDto(savedChannel);
    }

    @Override
    public ServerChannelDto updateChannel(Long serverId, Long channelId, UpdateChannelRequest request) {
        // Verify server exists and current user is owner
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Server", "id", serverId));

        Long currentUserId = securityUtils.getCurrentUserId();
        if (!server.getOwner().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.AUTH_403_FORBIDDEN, "Only server owner can update channels");
        }

        ServerChannel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel", "id", channelId));

        // Validate channel belongs to the specified server
        if (!channel.getServer().getId().equals(serverId)) {
            throw new BusinessException(ErrorCode.VAL_400_BAD_REQUEST, "Channel does not belong to this server");
        }

        // Update fields
        if (request.getName() != null) {
            channel.setName(request.getName());
        }
        if (request.getType() != null) {
            channel.setType(request.getType());
        }
        if (request.getPosition() != null) {
            channel.setPosition(request.getPosition());
        }
        if (request.getBitrate() != null) {
            channel.setBitrate(request.getBitrate());
        }
        if (request.getUserLimit() != null) {
            channel.setUserLimit(request.getUserLimit());
        }
        if (request.getNsfw() != null) {
            channel.setNsfw(request.getNsfw());
        }
        
        // Handle parent channel change
        if (request.getParentId() != null) {
            ServerChannel parentChannel = channelRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Channel", "parentId", request.getParentId()));
            
            if (!parentChannel.getServer().getId().equals(serverId)) {
                throw new BusinessException(ErrorCode.VAL_400_BAD_REQUEST, "Parent channel must belong to the same server");
            }
            
            channel.setParent(parentChannel);
        }

        ServerChannel updatedChannel = channelRepository.save(channel);
        log.info("Updated channel '{}' in server {}", updatedChannel.getName(), serverId);

        return mapToDto(updatedChannel);
    }

    @Override
    public void deleteChannel(Long serverId, Long channelId) {
        // Verify server exists and current user is owner
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Server", "id", serverId));

        Long currentUserId = securityUtils.getCurrentUserId();
        if (!server.getOwner().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.AUTH_403_FORBIDDEN, "Only server owner can delete channels");
        }

        ServerChannel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel", "id", channelId));

        // Validate channel belongs to the specified server
        if (!channel.getServer().getId().equals(serverId)) {
            throw new BusinessException(ErrorCode.VAL_400_BAD_REQUEST, "Channel does not belong to this server");
        }

        channelRepository.delete(channel);
        log.info("Deleted channel '{}' from server {}", channel.getName(), serverId);
    }

    /**
     * Map ServerChannel entity to DTO.
     */
    private ServerChannelDto mapToDto(ServerChannel channel) {
        return ServerChannelDto.builder()
                .id(channel.getId())
                .serverId(channel.getServer().getId())
                .name(channel.getName())
                .type(channel.getType())
                .parentId(channel.getParent() != null ? channel.getParent().getId() : null)
                .position(channel.getPosition())
                .bitrate(channel.getBitrate())
                .userLimit(channel.getUserLimit())
                .nsfw(channel.isNsfw())
                .build();
    }
}
