package com.novacore.server.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Invite data returned when creating an invite or resolving by code.
 */
@Data
@Builder
public class InviteDto {

    private String code;
    private String inviteLink;
    private Long serverId;
    private String serverName;
    private Long createdById;
    private String createdByUsername;
    private Integer uses;
    private Integer maxUses;
    private LocalDateTime expireAt;
    private Boolean temporary;
}
