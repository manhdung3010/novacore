package com.novacore.server.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ServerDto {

    private Long id;
    private String name;
    private Long ownerId;
    private String ownerUsername;
    private String iconUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

