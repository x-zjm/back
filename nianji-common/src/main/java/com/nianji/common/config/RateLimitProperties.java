package com.nianji.common.config;

import com.nianji.common.constant.RateLimitConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 限流配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    /**
     * 是否启用限流
     */
    private boolean enabled = true;

    /**
     * 默认限流配置
     */
    private RateLimitConfig defaultConfig = new RateLimitConfig(
            RateLimitConstants.DEFAULT_MAX_REQUESTS,
            RateLimitConstants.DEFAULT_TIME_WINDOW
    );

    /**
     * 自定义限流配置
     */
    private Map<String, RateLimitConfig> customConfigs = new HashMap<>();

    /**
     * 获取限流配置
     */
    public RateLimitConfig getConfig(RateLimitConstants.RateLimitType type) {
        String key = type.name().toLowerCase();
        return customConfigs.getOrDefault(key, getDefaultConfigForType(type));
    }

    private RateLimitConfig getDefaultConfigForType(RateLimitConstants.RateLimitType type) {
        return switch (type) {
            // 认证模块
            case LOGIN_IP -> new RateLimitConfig(RateLimitConstants.LOGIN_IP_LIMIT, RateLimitConstants.LOGIN_IP_WINDOW);
            case LOGIN_USER ->
                    new RateLimitConfig(RateLimitConstants.LOGIN_USER_LIMIT, RateLimitConstants.LOGIN_USER_WINDOW);
            case REGISTER_IP ->
                    new RateLimitConfig(RateLimitConstants.REGISTER_IP_LIMIT, RateLimitConstants.REGISTER_IP_WINDOW);
            case REFRESH_TOKEN ->
                    new RateLimitConfig(RateLimitConstants.REFRESH_TOKEN_LIMIT, RateLimitConstants.REFRESH_TOKEN_WINDOW);
            case VERIFY_CODE_IP ->
                    new RateLimitConfig(RateLimitConstants.VERIFY_CODE_IP_LIMIT, RateLimitConstants.VERIFY_CODE_IP_WINDOW);
            case VERIFY_CODE_TARGET ->
                    new RateLimitConfig(RateLimitConstants.VERIFY_CODE_TARGET_LIMIT, RateLimitConstants.VERIFY_CODE_TARGET_WINDOW);

            // Diary 模块配置
            case DIARY_CREATE_IP ->
                    new RateLimitConfig(RateLimitConstants.DIARY_CREATE_IP_LIMIT, RateLimitConstants.DIARY_CREATE_IP_WINDOW);
            case DIARY_CREATE_USER ->
                    new RateLimitConfig(RateLimitConstants.DIARY_CREATE_USER_LIMIT, RateLimitConstants.DIARY_CREATE_USER_WINDOW);
            case DIARY_UPDATE_IP ->
                    new RateLimitConfig(RateLimitConstants.DIARY_UPDATE_IP_LIMIT, RateLimitConstants.DIARY_UPDATE_IP_WINDOW);
            case DIARY_UPDATE_USER ->
                    new RateLimitConfig(RateLimitConstants.DIARY_UPDATE_USER_LIMIT, RateLimitConstants.DIARY_UPDATE_USER_WINDOW);
            case DIARY_DELETE_IP ->
                    new RateLimitConfig(RateLimitConstants.DIARY_DELETE_IP_LIMIT, RateLimitConstants.DIARY_DELETE_IP_WINDOW);
            case DIARY_DELETE_USER ->
                    new RateLimitConfig(RateLimitConstants.DIARY_DELETE_USER_LIMIT, RateLimitConstants.DIARY_DELETE_USER_WINDOW);
            case DIARY_QUERY_IP ->
                    new RateLimitConfig(RateLimitConstants.DIARY_QUERY_IP_LIMIT, RateLimitConstants.DIARY_QUERY_IP_WINDOW);
            case DIARY_QUERY_USER ->
                    new RateLimitConfig(RateLimitConstants.DIARY_QUERY_USER_LIMIT, RateLimitConstants.DIARY_QUERY_USER_WINDOW);
            case DIARY_LIST_IP ->
                    new RateLimitConfig(RateLimitConstants.DIARY_LIST_IP_LIMIT, RateLimitConstants.DIARY_LIST_IP_WINDOW);
            case DIARY_LIST_USER ->
                    new RateLimitConfig(RateLimitConstants.DIARY_LIST_USER_LIMIT, RateLimitConstants.DIARY_LIST_USER_WINDOW);

            // 通用接口
            case API_IP -> new RateLimitConfig(RateLimitConstants.API_IP_LIMIT, RateLimitConstants.API_IP_WINDOW);
            case API_USER -> new RateLimitConfig(RateLimitConstants.API_USER_LIMIT, RateLimitConstants.API_USER_WINDOW);


            default -> defaultConfig;
        };
    }

    /**
     * 限流配置类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateLimitConfig {
        private long limit;
        private long window;
    }
}