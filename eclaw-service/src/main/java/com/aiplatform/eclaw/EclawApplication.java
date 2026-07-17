package com.aiplatform.eclaw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.aiplatform.eclaw.entity")
@EnableJpaRepositories("com.aiplatform.eclaw.repository")
public class EclawApplication {
    public static void main(String[] args) {
        SpringApplication.run(EclawApplication.class, args);
    }
}
