package com.novacore.server.controller;

import com.novacore.server.dto.CreateInviteRequest;
import com.novacore.server.dto.CreateServerRequest;
import com.novacore.server.dto.InviteDto;
import com.novacore.server.dto.JoinServerRequest;
import com.novacore.server.dto.JoinServerResponse;
import com.novacore.server.dto.ServerDto;
import com.novacore.server.dto.ServerListFilter;
import com.novacore.server.dto.ServerListResponse;
import com.novacore.server.dto.UpdateServerRequest;
import com.novacore.server.service.ServerInviteService;
import com.novacore.server.service.ServerService;
import com.novacore.shared.constants.ApiConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.SERVERS_ENDPOINT)
@RequiredArgsConstructor
public class ServerController {

    private final ServerService serverService;
    private final ServerInviteService serverInviteService;

    @PostMapping
    public ResponseEntity<ServerDto> createServer(@Valid @RequestBody CreateServerRequest request) {
        ServerDto serverDto = serverService.createServer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(serverDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ServerDto> updateServer(
            @PathVariable Long id,
            @Valid @RequestBody UpdateServerRequest request) {
        ServerDto serverDto = serverService.updateServer(id, request);
        return ResponseEntity.ok(serverDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServerDto> getServer(@PathVariable Long id) {
        ServerDto serverDto = serverService.getServer(id);
        return ResponseEntity.ok(serverDto);
    }

    @GetMapping
    public ResponseEntity<ServerListResponse> getServers(@ModelAttribute ServerListFilter filter) {
        ServerListResponse response = serverService.getServers(filter != null ? filter : ServerListFilter.builder().build());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<JoinServerResponse> joinServer(
            @PathVariable Long id,
            @RequestBody(required = false) JoinServerRequest request) {
        JoinServerResponse response = serverService.joinServer(id, request);
        return response.isJoined()
                ? ResponseEntity.status(HttpStatus.CREATED).body(response)
                : ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("/{id}/invites")
    public ResponseEntity<InviteDto> createInvite(
            @PathVariable Long id,
            @RequestBody(required = false) @Valid CreateInviteRequest request) {
        InviteDto dto = serverInviteService.createInvite(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}

