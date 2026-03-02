package com.novacore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = RedisRepositoriesAutoConfiguration.class)
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.novacore")
public class NovaCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovaCoreApplication.class, args);
    }
}

