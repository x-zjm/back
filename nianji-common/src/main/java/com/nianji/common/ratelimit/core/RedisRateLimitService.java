package com.nianji.common.ratelimit.core;

import cn.hutool.core.util.ObjectUtil;
import com.nianji.common.ratelimit.api.RateLimitService;
import com.nianji.common.constant.CacheKeys;
import com.nianji.common.utils.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis限流服务实现 - 优化版
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisRateLimitService implements RateLimitService {

    private final CacheUtil cacheUtil;

    @Override
    public boolean isAllowed(String key, long limit, long window) {
        String rateLimitKey = buildRateLimitKey(key);

        try {
            Long current = cacheUtil.increment(rateLimitKey, 1);
            if (current == null) {
                log.warn("限流计数获取失败: {}", rateLimitKey);
                return true; // 限流服务异常时放行
            }

            if (current == 1) {
                // 第一次设置过期时间
                cacheUtil.expire(rateLimitKey, window, TimeUnit.SECONDS);
            }

            boolean allowed = current <= limit;

            if (!allowed) {
                log.warn("请求被限流 - Key: {}, 当前计数: {}, 限制: {}/{}秒",
                        key, current, limit, window);
            }

            return allowed;

        } catch (Exception e) {
            log.error("限流检查异常 - Key: {}", key, e);
            return true; // 限流服务异常时放行
        }
    }

    @Override
    public long getRemainingRequests(String key, long limit, long window) {
        String rateLimitKey = buildRateLimitKey(key);
        try {
            Long current = cacheUtil.increment(rateLimitKey, 0); // 不增加计数，只获取当前值
            return current != null ? Math.max(0, limit - current) : limit;
        } catch (Exception e) {
            log.error("获取剩余请求次数异常 - Key: {}", key, e);
            return limit;
        }
    }

    @Override
    public long getResetTime(String key, long window) {
        String rateLimitKey = buildRateLimitKey(key);
        try {
            long expire = cacheUtil.getExpire(rateLimitKey, TimeUnit.SECONDS);
            return ObjectUtil.isNotEmpty(expire) && expire > 0 ? expire : window;
        } catch (Exception e) {
            log.error("获取重置时间异常 - Key: {}", key, e);
            return window;
        }
    }

    @Override
    public void clearRateLimit(String key) {
        String rateLimitKey = buildRateLimitKey(key);
        try {
            cacheUtil.delete(rateLimitKey);
            log.debug("限流计数已清除 - Key: {}", key);
        } catch (Exception e) {
            log.error("清除限流计数异常 - Key: {}", key, e);
        }
    }

    /**
     * 构建限流键 - 优化版
     * 支持多种格式的key输入，提供更好的灵活性
     */
    private String buildRateLimitKey(String key) {
        // 方案1: 如果key已经是完整格式，直接使用
        if (key.startsWith(CacheKeys.PROJECT_PREFIX)) {
            return key;
        }

        // 方案2: 支持 "type:identifier" 格式
        String[] parts = key.split(":", 2);
        if (parts.length == 2) {
            String type = parts[0].trim();
            String identifier = parts[1].trim();

            // 验证type是否在预定义类型中
            if (isValidRateLimitType(type)) {
                return CacheKeys.Security.rateLimit(type, identifier);
            }
        }

        // 方案3: 默认使用"default"类型
        return CacheKeys.Security.rateLimit("default", key);
    }

    /**
     * 验证限流类型是否有效
     */
    private boolean isValidRateLimitType(String type) {
        // 预定义的限流类型
        return switch (type.toLowerCase()) {
            case "login_ip", "login_user", "register_ip", "refresh_token",
                 "api_ip", "api_user", "sms", "email", "default" -> true;
            default -> false;
        };
    }

    /**
     * 便捷方法：构建特定类型的限流键
     */
    public String buildTypedRateLimitKey(String type, String identifier) {
        return CacheKeys.Security.rateLimit(type, identifier);
    }

    /**
     * 便捷方法：构建登录IP限流键
     */
    public String buildLoginIpRateLimitKey(String ip) {
        return CacheKeys.Security.rateLimit("login_ip", ip);
    }

    /**
     * 便捷方法：构建登录用户限流键
     */
    public String buildLoginUserRateLimitKey(String username) {
        return CacheKeys.Security.rateLimit("login_user", username.toLowerCase());
    }

    /**
     * 便捷方法：构建注册IP限流键
     */
    public String buildRegisterIpRateLimitKey(String ip) {
        return CacheKeys.Security.rateLimit("register_ip", ip);
    }

    /**
     * 便捷方法：构建刷新令牌限流键
     */
    public String buildRefreshTokenRateLimitKey(String identifier) {
        return CacheKeys.Security.rateLimit("refresh_token", identifier);
    }
}