package com.nianji.diary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DiaryApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiaryApplication.class, args);
    }
}