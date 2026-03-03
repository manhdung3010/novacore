package com.novacore.server.dto;

import com.novacore.server.domain.ChannelType;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for ServerChannel - used in API responses.
 */
@Data
@Builder
public class ServerChannelDto {

    private Long id;
    private Long serverId;
    private String name;
    private ChannelType type;
    private Long parentId;
    private int position;
    private Integer bitrate;
    private Integer userLimit;
    private boolean nsfw;
}
