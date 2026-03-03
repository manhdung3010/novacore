package com.novacore.server.service;

import com.novacore.server.dto.CreateChannelRequest;
import com.novacore.server.dto.ServerChannelDto;
import com.novacore.server.dto.UpdateChannelRequest;

import java.util.List;

/**
 * Service interface for managing server channels (similar to Discord guild channels).
 */
public interface ServerChannelService {

    /**
     * Get all channels for a specific server, sorted by position.
     *
     * @param serverId the server ID
     * @return list of channels
     */
    List<ServerChannelDto> getChannelsByServerId(Long serverId);

    /**
     * Get a specific channel by ID.
     *
     * @param serverId  the server ID (for validation)
     * @param channelId the channel ID
     * @return the channel DTO
     */
    ServerChannelDto getChannel(Long serverId, Long channelId);

    /**
     * Create a new channel in the server.
     * Only server owner or admin can create channels.
     *
     * @param serverId the server ID
     * @param request  the creation request
     * @return the created channel DTO
     */
    ServerChannelDto createChannel(Long serverId, CreateChannelRequest request);

    /**
     * Update an existing channel.
     * Only server owner or admin can update channels.
     *
     * @param serverId  the server ID
     * @param channelId the channel ID
     * @param request   the update request
     * @return the updated channel DTO
     */
    ServerChannelDto updateChannel(Long serverId, Long channelId, UpdateChannelRequest request);

    /**
     * Delete a channel.
     * Only server owner or admin can delete channels.
     *
     * @param serverId  the server ID
     * @param channelId the channel ID
     */
    void deleteChannel(Long serverId, Long channelId);
}
