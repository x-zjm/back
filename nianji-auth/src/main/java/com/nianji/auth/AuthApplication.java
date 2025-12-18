package com.nianji.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@EnableAsync
@SpringBootApplication
@EnableDiscoveryClient
// @Import({com.nianji.common.config.MyBatisPlusConfig.class
//         , com.nianji.common.config.AsyncConfig.class
//         , com.nianji.common.config.JacksonConfig.class
//         , com.nianji.common.config.RedisConfig.class
// })
// @ComponentScan({"com.nianji.common.config", "com.nianji.common.mybatis"})
@ComponentScan(
        basePackages = {
                "com.nianji.common",
                "com.nianji.auth"
        }
        // ,
        // // 排除可能冲突的配置类
        // excludeFilters = {
        //         @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        //                 value = {
        //                         MyBatisPlusConfig.class,
        //                         AsyncConfig.class,
        //                         JacksonConfig.class,
        //                         RedisConfig.class
        //                 })
        // }
)
public class AuthApplication {
    public static void main(String[] args) {
        log.info("AuthApplication start.");
        SpringApplication.run(AuthApplication.class, args);
        log.info("AuthApplication start success.");
    }
}