package com.novacore.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatWsSendMessage {

    @NotNull
    private Long serverId;

    @NotNull
    private Long channelId;

    @NotBlank
    private String content;
}

