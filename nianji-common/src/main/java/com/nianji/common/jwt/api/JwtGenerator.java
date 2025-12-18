package com.nianji.common.jwt.api;

import com.nianji.common.jwt.exception.JwtGenerationException;

/**
 * JWT令牌生成器接口
 * 用于认证服务生成各种类型的JWT令牌
 */
public interface JwtGenerator {
    
    /**
     * 生成访问令牌
     */
    String generateAccessToken(String username, Long userId);
    
    /**
     * 生成包含CSRF Token的访问令牌
     */
    String generateAccessTokenWithCsrf(String username, Long userId, String csrfToken);
    
    /**
     * 生成刷新令牌
     */
    String generateRefreshToken(String username, Long userId);
    
    /**
     * 生成短期令牌（用于一次性操作）
     */
    String generateShortLivedToken(String username, Long userId, int minutes);
    
    /**
     * 生成CSRF Token
     */
    String generateCsrfToken();
    
    /**
     * 验证刷新令牌并生成新的访问令牌
     */
    String validateAndRefresh(String refreshToken) throws JwtGenerationException;
}