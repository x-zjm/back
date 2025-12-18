package com.nianji.gateway.service;

import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.ExceptionFactory;
import com.nianji.common.exception.client.AuthenticationException;
import com.nianji.common.jwt.api.JwtValidator;
import com.nianji.common.jwt.dto.JwtUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 网关JWT服务 - 集成异常体系
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GatewayJwtService {

    private final JwtValidator jwtValidator;

    /**
     * 验证Token并获取用户信息 - 集成异常体系
     */
    public JwtUserInfo validateAndGetUserInfo(String token) {
        try {
            // 基础验证
            if (!jwtValidator.validateToken(token)) {
                throw ExceptionFactory.authentication(
                    ErrorCode.Client.TOKEN_INVALID, 
                    "Token验证失败"
                );
            }

            // 解析用户信息
            JwtUserInfo userInfo = jwtValidator.validateAndGetUserInfo(token);
            if (userInfo == null || !userInfo.isValid()) {
                throw ExceptionFactory.authentication(
                    ErrorCode.Client.TOKEN_INVALID, 
                    "用户信息无效"
                );
            }

            // 检查Token是否过期
            if (userInfo.isExpired()) {
                throw ExceptionFactory.authentication(
                    ErrorCode.Client.TOKEN_EXPIRED, 
                    "Token已过期"
                );
            }

            log.debug("✅ JWT Token验证成功 - 用户: {}", userInfo.getUsername());
            return userInfo;

        } catch (Exception e) {
            log.error("JWT Token验证异常", e);
            throw ExceptionFactory.authentication(
                ErrorCode.Client.TOKEN_INVALID, 
                "Token验证异常"
            );
        }
    }

    /**
     * 快速验证Token有效性
     */
    public boolean quickValidateToken(String token) {
        try {
            return jwtValidator.validateToken(token) && !isTokenBlacklisted(token);
        } catch (Exception e) {
            log.debug("快速Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查Token是否在黑名单中（同步版本）
     */
    private boolean isTokenBlacklisted(String token) {
        // 这里需要根据您的实际缓存实现来调整
        // 如果是同步操作，可能需要使用非响应式RedisTemplate
        return false; // 临时返回false，实际需要实现
    }
}