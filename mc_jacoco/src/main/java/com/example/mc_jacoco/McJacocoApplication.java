package com.example.mc_jacoco;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableScheduling
@EnableAsync
@SpringBootApplication
public class McJacocoApplication {
    public static void main(String[] args) {
        log.info("【Springboot启动成功】");
        SpringApplication.run(McJacocoApplication.class, args);
    }

}
