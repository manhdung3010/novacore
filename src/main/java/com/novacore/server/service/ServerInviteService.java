package com.novacore.server.service;

import com.novacore.server.dto.AcceptInviteResponse;
import com.novacore.server.dto.CreateInviteRequest;
import com.novacore.server.dto.InviteDto;

/**
 * Service for server invites: create, resolve by code, accept invite.
 */
public interface ServerInviteService {

    /**
     * Resolve invite by code (public). Returns server info and invite metadata.
     */
    InviteDto resolveInvite(String code);

    /**
     * Current user accepts invite by code. Joins directly or creates join request depending on server settings.
     */
    AcceptInviteResponse acceptInvite(String code);

    /**
     * Create a new invite for the server. Caller must be owner or member (according to allow_invite_role).
     */
    InviteDto createInvite(Long serverId, CreateInviteRequest request);
}
