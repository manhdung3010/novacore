package com.novacore.server.service;

import com.novacore.server.dto.CreateServerRequest;
import com.novacore.server.dto.JoinServerRequest;
import com.novacore.server.dto.JoinServerResponse;
import com.novacore.server.dto.ServerDto;
import com.novacore.server.dto.ServerListFilter;
import com.novacore.server.dto.ServerListResponse;
import com.novacore.server.dto.UpdateServerRequest;

import java.util.List;

public interface ServerService {

    ServerDto createServer(CreateServerRequest request);

    /**
     * Current user requests to join a server. If require_approval then creates join request (202);
     * otherwise adds as member (201).
     */
    JoinServerResponse joinServer(Long serverId, JoinServerRequest request);

    ServerDto updateServer(Long id, UpdateServerRequest request);

    /**
     * Get one server by id.
     */
    ServerDto getServer(Long id);

    /**
     * Get servers where current user is owner (created) or member (joined), with filter and pagination.
     */
    ServerListResponse getMyServers(ServerListFilter filter);

    /**
     * Get all servers (all servers in the system).
     */
    List<ServerDto> getAllServers();

    /**
     * Get servers the current user has not joined (not owner, not member), with filter and pagination.
     */
    ServerListResponse getServersNotJoined(ServerListFilter filter);

    /**
     * List servers. joined comes from filter (true = my servers, false = discover).
     */
    ServerListResponse getServers(ServerListFilter filter);
}

