package com.novacore.health.controller;

import com.novacore.shared.constants.ApiConstants;
import com.novacore.shared.response.ApiResponse;
import com.novacore.shared.response.ApiResponseBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(ApiConstants.API_V1_BASE)
public class HealthController {

    @GetMapping(ApiConstants.HEALTH_ENDPOINT)
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        Map<String, String> healthStatus = Map.of(
                "status", "UP",
                "service", "NovaCore Backend",
                "version", "1.0.0"
        );
        
        return ResponseEntity.ok(ApiResponseBuilder.success("Service is healthy", healthStatus));
    }
}
