package com.novacore.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;

/**
 * Request body for POST /servers/{id}/join (optional message when require_approval is true).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinServerRequest {

    @Size(max = 1000)
    private String message;
}
