package com.novacore.chat.controller;

import com.novacore.auth.util.SecurityUtils;
import com.novacore.chat.dto.ChatMessageEvent;
import com.novacore.chat.dto.SendMessageRequest;
import com.novacore.chat.service.ChatRealtimeService;
import com.novacore.shared.constants.ApiConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.API_V1_BASE)
@RequiredArgsConstructor
public class ChatRealtimeController {

    private final ChatRealtimeService chatRealtimeService;
    private final SecurityUtils securityUtils;

    @PostMapping("/servers/{serverId}/channels/{channelId}/messages/realtime")
    public ResponseEntity<ChatMessageEvent> sendRealtimeMessage(
            @PathVariable Long serverId,
            @PathVariable Long channelId,
            @Valid @RequestBody SendMessageRequest request) {
        Long authorId = securityUtils.getCurrentUserId();
        ChatMessageEvent event = chatRealtimeService.sendMessage(serverId, channelId, authorId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(event);
    }
}

