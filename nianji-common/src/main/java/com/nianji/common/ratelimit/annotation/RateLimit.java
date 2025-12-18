package com.nianji.common.ratelimit.annotation;

import com.nianji.common.constant.RateLimitConstants;

import java.lang.annotation.*;

/**
 * 限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(RateLimits.class)
public @interface RateLimit {

    /**
     * 限流类型
     */
    RateLimitConstants.RateLimitType type();

    /**
     * 限流键（支持SpEL表达式） 例如：#request.ip, #user.id
     */
    String key() default "";

    /**
     * 最大请求次数（覆盖配置中的值）
     */
    long limit() default -1;

    /**
     * 时间窗口（秒，覆盖配置中的值）
     */
    long window() default -1;

    /**
     * 限流提示信息
     */
    String message() default "请求过于频繁，请稍后重试";
}