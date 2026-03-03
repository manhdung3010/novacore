package com.novacore.server.controller;

import com.novacore.server.dto.CreateChannelRequest;
import com.novacore.server.dto.ServerChannelDto;
import com.novacore.server.dto.UpdateChannelRequest;
import com.novacore.server.service.ServerChannelService;
import com.novacore.shared.constants.ApiConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing server channels.
 * Provides endpoints to list, create, update, and delete channels within a server.
 * Design pattern: Discord-style, where channels are fetched separately from server detail.
 */
@RestController
@RequestMapping(ApiConstants.SERVERS_ENDPOINT + "/{serverId}/channels")
@RequiredArgsConstructor
public class ServerChannelController {

    private final ServerChannelService channelService;

    /**
     * Get all channels for a specific server.
     * Channels are sorted by position.
     *
     * @param serverId the server ID
     * @return list of channels
     */
    @GetMapping
    public ResponseEntity<List<ServerChannelDto>> getChannels(@PathVariable Long serverId) {
        List<ServerChannelDto> channels = channelService.getChannelsByServerId(serverId);
        return ResponseEntity.ok(channels);
    }

    /**
     * Get a specific channel by ID.
     *
     * @param serverId  the server ID
     * @param channelId the channel ID
     * @return the channel details
     */
    @GetMapping("/{channelId}")
    public ResponseEntity<ServerChannelDto> getChannel(
            @PathVariable Long serverId,
            @PathVariable Long channelId) {
        ServerChannelDto channel = channelService.getChannel(serverId, channelId);
        return ResponseEntity.ok(channel);
    }

    /**
     * Create a new channel in the server.
     * Only the server owner can create channels.
     *
     * @param serverId the server ID
     * @param request  the channel creation request
     * @return the created channel
     */
    @PostMapping
    public ResponseEntity<ServerChannelDto> createChannel(
            @PathVariable Long serverId,
            @Valid @RequestBody CreateChannelRequest request) {
        ServerChannelDto channel = channelService.createChannel(serverId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(channel);
    }

    /**
     * Update an existing channel.
     * Only the server owner can update channels.
     *
     * @param serverId  the server ID
     * @param channelId the channel ID
     * @param request   the update request
     * @return the updated channel
     */
    @PatchMapping("/{channelId}")
    public ResponseEntity<ServerChannelDto> updateChannel(
            @PathVariable Long serverId,
            @PathVariable Long channelId,
            @Valid @RequestBody UpdateChannelRequest request) {
        ServerChannelDto channel = channelService.updateChannel(serverId, channelId, request);
        return ResponseEntity.ok(channel);
    }

    /**
     * Delete a channel.
     * Only the server owner can delete channels.
     *
     * @param serverId  the server ID
     * @param channelId the channel ID
     */
    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteChannel(
            @PathVariable Long serverId,
            @PathVariable Long channelId) {
        channelService.deleteChannel(serverId, channelId);
        return ResponseEntity.noContent().build();
    }
}
