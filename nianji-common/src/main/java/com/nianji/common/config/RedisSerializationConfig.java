package com.nianji.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 序列化工具类 提供统一的序列化配置
 */
public class RedisSerializationConfig {

    private RedisSerializationConfig() {
        // 工具类，防止实例化
    }

    // 使用单例模式确保序列化器完全一致
    private static final ObjectMapper REDIS_OBJECT_MAPPER = createRedisObjectMapper();
    private static final GenericJackson2JsonRedisSerializer VALUE_SERIALIZER =
            new GenericJackson2JsonRedisSerializer(REDIS_OBJECT_MAPPER);
    private static final StringRedisSerializer KEY_SERIALIZER = new StringRedisSerializer();

    /**
     * 创建专门用于Redis序列化的ObjectMapper
     */
    public static ObjectMapper createRedisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册JavaTime模块，支持Java8时间API
        objectMapper.registerModule(new JavaTimeModule());
        // 禁用日期作为时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 启用类型信息
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return objectMapper;
    }

    /**
     * 获取通用的Key序列化器
     */
    public static StringRedisSerializer keySerializer() {
        return KEY_SERIALIZER;
    }

    /**
     * 获取通用的Value序列化器
     */
    public static GenericJackson2JsonRedisSerializer valueSerializer() {
        return VALUE_SERIALIZER;
    }

    /**
     * 获取Redis ObjectMapper单例
     */
    public static ObjectMapper getRedisObjectMapper() {
        return REDIS_OBJECT_MAPPER;
    }
}