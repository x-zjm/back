package com.nianji.common.config;

import com.nianji.common.constant.CacheKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableCaching
public class SpringCacheConfig {

    public SpringCacheConfig() {
        log.debug("SpringCacheConfig 被初始化了");
    }

    /**
     * Redis 缓存管理器 使用与 RedisTemplate 完全相同的序列化配置
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory, CacheConfig cacheConfig) {
        log.debug("初始化 Redis CacheManager...");

        // 使用统一的序列化器
        StringRedisSerializer keySerializer = RedisSerializationConfig.keySerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = RedisSerializationConfig.valueSerializer();

        // 默认缓存配置（30分钟）
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(CacheKeys.Expire.MEDIUM))  // 使用统一的过期时间常量
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> CacheKeys.PROJECT_PREFIX + ":" + cacheName + ":");

        // 从 CacheKeys 获取所有 Spring Cache 配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        Map<String, Long> expireConfig = cacheConfig.getSpringCacheConfigs();

        for (Map.Entry<String, Long> entry : expireConfig.entrySet()) {
            cacheConfigurations.put(
                    entry.getKey(),
                    createCacheConfiguration(entry.getValue(), keySerializer, valueSerializer)
            );
        }

        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();

        log.info("Redis CacheManager 初始化完成，已配置 {} 个缓存策略", cacheConfigurations.size());
        return cacheManager;
    }

    /**
     * 创建缓存配置
     */
    private RedisCacheConfiguration createCacheConfiguration(
            long ttlSeconds,
            StringRedisSerializer keySerializer,
            GenericJackson2JsonRedisSerializer valueSerializer) {

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(ttlSeconds))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> "");
    }

    /**
     * 自定义缓存异常处理器
     */
    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new CustomCacheErrorHandler();
    }

    /**
     * 自定义缓存异常处理
     */
    public static class CustomCacheErrorHandler extends SimpleCacheErrorHandler {
        @Override
        public void handleCacheGetError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
            log.warn("缓存获取失败，降级到数据源查询。缓存名称: {}, 键: {}", cache.getName(), key, exception);
            // 不抛出异常，让方法继续执行数据库查询
        }

        @Override
        public void handleCachePutError(RuntimeException exception, org.springframework.cache.Cache cache, Object key, Object value) {
            log.warn("缓存写入失败，不影响主流程。缓存名称: {}, 键: {}", cache.getName(), key, exception);
            // 不抛出异常，缓存写入失败不影响主流程
        }

        @Override
        public void handleCacheEvictError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
            log.warn("缓存删除失败。缓存名称: {}, 键: {}", cache.getName(), key, exception);
        }

        @Override
        public void handleCacheClearError(RuntimeException exception, org.springframework.cache.Cache cache) {
            log.warn("缓存清空失败。缓存名称: {}", cache.getName(), exception);
        }
    }
}