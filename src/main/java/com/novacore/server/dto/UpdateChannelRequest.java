package com.novacore.server.dto;

import com.novacore.server.domain.ChannelType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to update an existing channel.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateChannelRequest {

    @Size(min = 1, max = 128, message = "Channel name must be between 1 and 128 characters")
    private String name;

    private ChannelType type;

    private Long parentId;

    private Integer position;

    private Integer bitrate;

    private Integer userLimit;

    private Boolean nsfw;
}
