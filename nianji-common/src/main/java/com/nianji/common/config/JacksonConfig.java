package com.nianji.common.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Slf4j
@Configuration
public class JacksonConfig {

    public JacksonConfig() {
        log.debug("JacksonConfig 被初始化了");
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 注册 JavaTimeModule 支持 Java 8 时间 API
        objectMapper.registerModule(new JavaTimeModule());

        // 禁用日期作为时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 禁用未知属性失败
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 明确禁用默认类型信息 - 这是关键
        objectMapper.deactivateDefaultTyping();

        return objectMapper;
    }


    /**
     * 使用 Spring Boot 的定制器方式配置 Jackson
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            builder.modules(new JavaTimeModule());
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.failOnUnknownProperties(false);

            // 明确禁用默认类型
            builder.postConfigurer(objectMapper -> {
                objectMapper.deactivateDefaultTyping();
            });
        };
    }
}