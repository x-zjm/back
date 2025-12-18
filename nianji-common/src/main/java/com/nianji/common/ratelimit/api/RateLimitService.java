package com.nianji.common.ratelimit.api;

/**
 * 限流服务接口 - 放在 common 模块
 */
public interface RateLimitService {
    
    /**
     * 检查是否允许请求
     */
    boolean isAllowed(String key, long limit, long window);
    
    /**
     * 获取剩余请求次数
     */
    long getRemainingRequests(String key, long limit, long window);
    
    /**
     * 获取重置时间
     */
    long getResetTime(String key, long window);
    
    /**
     * 清理限流计数
     */
    void clearRateLimit(String key);
}