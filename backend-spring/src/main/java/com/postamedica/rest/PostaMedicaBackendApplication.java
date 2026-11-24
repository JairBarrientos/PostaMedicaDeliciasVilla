package com.postamedica.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PostaMedicaBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(PostaMedicaBackendApplication.class, args);
    }
}