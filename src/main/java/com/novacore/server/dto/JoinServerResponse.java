package com.novacore.server.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Response for POST /servers/{id}/join.
 * joined = true: user is now a member (201).
 * joined = false: join request created, pending approval (202).
 */
@Data
@Builder
public class JoinServerResponse {

    private boolean joined;
    private ServerDto server;
}
