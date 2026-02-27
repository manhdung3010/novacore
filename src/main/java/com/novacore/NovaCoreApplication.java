package com.novacore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NovaCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovaCoreApplication.class, args);
    }
}

