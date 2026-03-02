package com.novacore.server.mapper;

import com.novacore.server.domain.Server;
import com.novacore.server.dto.CreateServerRequest;
import com.novacore.server.dto.ServerDto;
import com.novacore.server.dto.UpdateServerRequest;
import com.novacore.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class ServerMapper {

    public Server toEntity(CreateServerRequest request, User owner) {
        return Server.builder()
                .name(request.getName())
                .owner(owner)
                .iconUrl(request.getIconUrl())
                .build();
    }

    public void updateEntity(Server server, UpdateServerRequest request) {
        if (request.getName() != null) {
            server.setName(request.getName());
        }
        if (request.getIconUrl() != null) {
            server.setIconUrl(request.getIconUrl());
        }
    }

    public ServerDto toDto(Server server) {
        return ServerDto.builder()
                .id(server.getId())
                .name(server.getName())
                .ownerId(server.getOwner() != null ? server.getOwner().getId() : null)
                .ownerUsername(server.getOwner() != null ? server.getOwner().getUsername() : null)
                .iconUrl(server.getIconUrl())
                .createdAt(server.getCreatedAt())
                .updatedAt(server.getUpdatedAt())
                .build();
    }
}

