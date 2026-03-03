package com.novacore.server.dto;

import com.novacore.server.domain.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to create a new channel in a server.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateChannelRequest {

    @NotBlank(message = "Channel name is required")
    @Size(min = 1, max = 128, message = "Channel name must be between 1 and 128 characters")
    private String name;

    @NotNull(message = "Channel type is required")
    private ChannelType type;

    private Long parentId;

    private int position;

    private Integer bitrate;

    private Integer userLimit;

    private boolean nsfw;
}
