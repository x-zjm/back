package com.nianji.common.ratelimit.annotation;

import com.nianji.common.constant.RateLimitConstants;

import java.lang.annotation.*;

/**
 * 全局限流注解（应用于Controller类）
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GlobalRateLimit {

    /**
     * 限流类型
     */
    RateLimitConstants.RateLimitType type() default RateLimitConstants.RateLimitType.API_IP;

    /**
     * 最大请求次数
     */
    long limit() default 100;

    /**
     * 时间窗口（秒）
     */
    long window() default 60;
}