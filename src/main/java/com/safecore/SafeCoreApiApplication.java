package com.safecore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SafeCoreApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SafeCoreApiApplication.class, args);
    }
}
