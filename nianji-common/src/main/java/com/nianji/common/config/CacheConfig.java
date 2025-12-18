package com.nianji.common.config;

import com.nianji.common.constant.CacheKeys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存配置管理器 - 最终简化版本
 */
@Component
@Data
@ConfigurationProperties(prefix = "cache")
public class CacheConfig {

    private boolean enabled = true;
    private long defaultExpire = CacheKeys.Expire.MEDIUM;
    private Map<String, Long> customExpires = new HashMap<>();

    /**
     * 获取 Spring Cache 配置
     */
    public Map<String, Long> getSpringCacheConfigs() {
        Map<String, Long> configs = CacheKeys.Config.getSpringCacheConfigs();

        // 用自定义配置覆盖默认配置
        for (Map.Entry<String, Long> entry : customExpires.entrySet()) {
            if (configs.containsKey(entry.getKey())) {
                configs.put(entry.getKey(), entry.getValue());
            }
        }

        return configs;
    }

    /**
     * 智能获取缓存过期时间
     */
    public long getExpire(String cacheKey) {
        // 1. 精确匹配自定义配置
        Long customExpire = customExpires.get(cacheKey);
        if (customExpire != null) {
            return customExpire;
        }

        // 2. 模式匹配自定义配置
        for (Map.Entry<String, Long> entry : customExpires.entrySet()) {
            if (cacheKey.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // 3. 根据键模式推断过期时间
        return CacheKeys.Config.inferExpire(cacheKey);
    }

    /**
     * 获取带时间单位的过期时间
     */
    public long getExpire(String cacheKey, TimeUnit timeUnit) {
        long seconds = getExpire(cacheKey);
        return timeUnit.convert(seconds, TimeUnit.SECONDS);
    }

    /**
     * 根据缓存名称获取过期时间 用于 Spring Cache 注解等场景
     */
    public long getExpireForCacheName(String cacheName) {
        // 1. 检查自定义配置
        Long customExpire = customExpires.get(cacheName);
        if (customExpire != null) {
            return customExpire;
        }

        // 2. 使用预定义的默认过期时间
        return CacheKeys.Config.getExpireForCacheName(cacheName);
    }

    /**
     * 获取带时间单位的过期时间（缓存名称版本）
     */
    public long getExpireForCacheName(String cacheName, TimeUnit timeUnit) {
        long seconds = getExpireForCacheName(cacheName);
        return timeUnit.convert(seconds, TimeUnit.SECONDS);
    }

}