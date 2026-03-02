package com.novacore.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateServerRequest {

    @NotBlank
    @Size(max = 128)
    private String name;

    @Size(max = 1024)
    private String iconUrl;
}

