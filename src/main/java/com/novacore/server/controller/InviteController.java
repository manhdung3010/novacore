package com.novacore.server.controller;

import com.novacore.server.dto.AcceptInviteResponse;
import com.novacore.server.dto.InviteDto;
import com.novacore.server.service.ServerInviteService;
import com.novacore.shared.constants.ApiConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.INVITES_ENDPOINT)
@RequiredArgsConstructor
public class InviteController {

    private final ServerInviteService serverInviteService;

    /**
     * Resolve invite by code (public, no auth required).
     * Returns server info and invite metadata for preview.
     */
    @GetMapping("/{code}")
    public ResponseEntity<InviteDto> resolveInvite(@PathVariable String code) {
        InviteDto dto = serverInviteService.resolveInvite(code);
        return ResponseEntity.ok(dto);
    }

    /**
     * Accept invite (authenticated). Joins server directly or creates join request.
     */
    @PostMapping("/{code}/accept")
    public ResponseEntity<AcceptInviteResponse> acceptInvite(@PathVariable String code) {
        AcceptInviteResponse response = serverInviteService.acceptInvite(code);
        return response.isJoined()
                ? ResponseEntity.status(HttpStatus.CREATED).body(response)
                : ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
