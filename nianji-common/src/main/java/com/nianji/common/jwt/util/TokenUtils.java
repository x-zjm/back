package com.nianji.common.jwt.util;

import org.springframework.util.StringUtils;

/**
 * Token 工具类
 */
public class TokenUtils {

    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 从 Authorization 头中提取 token
     *
     * @param authorizationHeader
     *         Authorization 头
     * @return 提取后的 token，如果没有 Bearer 前缀则返回原值
     */
    public static String extractToken(String authorizationHeader) {
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }
        return authorizationHeader;
    }

    /**
     * 从 Authorization 头中提取 token，如果没有则返回 null
     */
    public static String extractTokenOrNull(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)) {
            return null;
        }
        return extractToken(authorizationHeader);
    }

    /**
     * 验证 token 格式是否有效（不包含 Bearer 前缀）
     */
    public static boolean isValidTokenFormat(String token) {
        return StringUtils.hasText(token) && !token.startsWith(BEARER_PREFIX);
    }

    /**
     * 为 token 添加 Bearer 前缀
     */
    public static String addBearerPrefix(String token) {
        if (StringUtils.hasText(token) && !token.startsWith(BEARER_PREFIX)) {
            return BEARER_PREFIX + token;
        }
        return token;
    }
}