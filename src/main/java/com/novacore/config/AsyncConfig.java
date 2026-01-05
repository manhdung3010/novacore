package com.novacore.config;

import com.novacore.shared.context.RequestContextHolder;
import com.novacore.shared.context.RequestContextTaskDecorator;
import com.novacore.config.properties.AsyncProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@EnableAsync
@RequiredArgsConstructor
@EnableConfigurationProperties(AsyncProperties.class)
public class AsyncConfig implements AsyncConfigurer {

    private final AsyncProperties asyncProperties;

    @Bean(name = "contextAwareTaskExecutor")
    public Executor contextAwareTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncProperties.getCorePoolSize());
        executor.setMaxPoolSize(asyncProperties.getMaxPoolSize());
        executor.setQueueCapacity(asyncProperties.getQueueCapacity());
        executor.setThreadNamePrefix(asyncProperties.getThreadNamePrefix());
        executor.setTaskDecorator(new RequestContextTaskDecorator());
        
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.setWaitForTasksToCompleteOnShutdown(
                asyncProperties.isWaitForTasksToCompleteOnShutdown());
        executor.setAwaitTerminationSeconds(
                asyncProperties.getAwaitTerminationSeconds());
        
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return contextAwareTaskExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            String requestId = RequestContextHolder.getRequestId();
            String traceId = RequestContextHolder.getTraceId();
            
            log.error("Async method '{}' failed [traceId={}, requestId={}] with parameters: {}", 
                method.getName(), traceId, requestId, params, throwable);
        };
    }
}
