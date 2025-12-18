package com.nianji.common.jwt.api;

import com.nianji.common.jwt.exception.JwtValidationException;
import com.nianji.common.jwt.dto.JwtUserInfo;
import com.nianji.common.jwt.dto.JwtDetails;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * JWT令牌验证器接口
 * 用于网关和服务端验证JWT令牌
 */
public interface JwtValidator {
    
    /**
     * 验证令牌是否有效
     */
    boolean validateToken(String token);
    
    /**
     * 验证令牌和CSRF Token
     */
    boolean validateTokenWithCsrf(String token, String expectedCsrfToken);
    
    /**
     * 验证令牌并返回用户信息
     */
    JwtUserInfo validateAndGetUserInfo(String token) throws JwtValidationException;
    
    /**
     * 提取用户名
     */
    String extractUsername(String token);
    
    /**
     * 提取用户ID
     */
    Long extractUserId(String token);
    
    /**
     * 提取令牌类型
     */
    String extractTokenType(String token);
    
    /**
     * 提取过期时间
     */
    Date extractExpiration(String token);
    
    /**
     * 检查令牌是否过期
     */
    boolean isTokenExpired(String token);
    
    /**
     * 检查令牌是否即将过期
     */
    boolean isTokenAboutToExpire(String token, long time, TimeUnit timeUnit);
    
    /**
     * 获取令牌剩余时间
     */
    long getRemainingTime(String token);
    
    /**
     * 获取令牌详细信息
     */
    JwtDetails getTokenDetails(String token);
}