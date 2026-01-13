package com.xaiforge.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.xaiforge")
@EntityScan(basePackages = "com.xaiforge.domain")
@EnableJpaRepositories(basePackages = "com.xaiforge.infrastructure.persistence")
public class XaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(XaiApplication.class, args);
    }
}

