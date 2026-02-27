package com.novacore.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for async task executor.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "async")
public class AsyncProperties {

    @NotNull(message = "Core pool size must be specified")
    @Min(value = 1, message = "Core pool size must be at least 1")
    private int corePoolSize = 5;

    @NotNull(message = "Max pool size must be specified")
    @Min(value = 1, message = "Max pool size must be at least 1")
    private int maxPoolSize = 10;

    @NotNull(message = "Queue capacity must be specified")
    @Min(value = 1, message = "Queue capacity must be at least 1")
    private int queueCapacity = 100;

    private String threadNamePrefix = "async-";

    private boolean waitForTasksToCompleteOnShutdown = true;

    @Min(value = 0, message = "Await termination seconds must be non-negative")
    private int awaitTerminationSeconds = 30;
}




















