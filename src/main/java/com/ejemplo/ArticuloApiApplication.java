package com.ejemplo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.ejemplo")
public class ArticuloApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArticuloApiApplication.class, args);
    }

}