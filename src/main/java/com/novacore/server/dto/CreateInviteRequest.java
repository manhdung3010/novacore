package com.novacore.server.dto;

import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

/**
 * Request body for POST /servers/{id}/invites.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInviteRequest {

    @Min(1)
    @Max(100_000)
    private Integer maxUses;

    @Future(message = "expireAt must be in the future")
    private LocalDateTime expireAt;

    @Builder.Default
    private Boolean temporary = false;
}
