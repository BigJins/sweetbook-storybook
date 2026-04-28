package com.sweetbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SweetbookApplication {
    public static void main(String[] args) {
        SpringApplication.run(SweetbookApplication.class, args);
    }
}
