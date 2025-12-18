package com.nianji.common.ratelimit.annotation;

import java.lang.annotation.*;

/**
 * RateLimit 容器注解
 * 用于支持在同一个方法上使用多个 @RateLimit 注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimits {
    
    /**
     * 多个限流注解
     */
    RateLimit[] value();
}