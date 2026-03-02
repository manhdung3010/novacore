package com.novacore.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paged response for server list APIs (my servers, discover).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerListResponse {

    private List<ServerDto> content;
    private long totalElements;
    private int totalPages;
    private int number;  // current page (0-based)
    private int size;
    private boolean first;
    private boolean last;
}
