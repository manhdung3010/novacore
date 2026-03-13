package com.novacore.chat.controller;

import com.novacore.auth.util.SecurityUtils;
import com.novacore.chat.dto.ChatMessageDto;
import com.novacore.chat.service.ChatMessageQueryService;
import com.novacore.shared.constants.ApiConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping(ApiConstants.API_V1_BASE)
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageQueryService chatMessageQueryService;
    private final SecurityUtils securityUtils;

    @GetMapping("/servers/{serverId}/channels/{channelId}/messages")
    public ResponseEntity<List<ChatMessageDto>> listChannelMessages(
            @PathVariable Long serverId,
            @PathVariable Long channelId,
            @RequestParam(required = false) Instant before,
            @RequestParam(defaultValue = "50") int limit) {

        Long requesterId = securityUtils.getCurrentUserId();
        List<ChatMessageDto> messages = chatMessageQueryService.listChannelMessages(
                serverId,
                channelId,
                requesterId,
                before,
                limit);
        return ResponseEntity.ok(messages);
    }
}

