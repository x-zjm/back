package com.nianji.auth.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.nianji.auth.context.RefreshTokenContext;
import com.nianji.auth.model.token.RefreshTokenMetadata;
import com.nianji.auth.service.RefreshTokenCacheService;
import com.nianji.auth.service.SessionManagementService;
import com.nianji.auth.service.TokenService;
import com.nianji.auth.service.UserCacheService;
import com.nianji.auth.vo.LoginVO;
import com.nianji.common.config.CacheConfig;
import com.nianji.common.constant.CacheKeys;
import com.nianji.common.enums.TokenTypeEnum;
import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.jwt.api.JwtGenerator;
import com.nianji.common.jwt.api.JwtValidator;
import com.nianji.common.reqres.BizResult;
import com.nianji.common.utils.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 令牌管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final JwtGenerator jwtGenerator;
    private final JwtValidator jwtValidator;
    private final UserCacheService userCacheService;
    private final RefreshTokenCacheService refreshTokenCacheService;
    private final SessionManagementService sessionManagementService;

    private final CacheUtil cacheUtil;
    private final CacheConfig cacheConfig;

    @Override
    public BizResult<Void> processLogout(String token) {
        Long userId = jwtValidator.extractUserId(token);
        String username = jwtValidator.extractUsername(token);

        if (userId != null) {
            // 1. 清除登录token缓存
            String tokenKey = CacheKeys.Auth.accessToken(userId);
            cacheUtil.deleteString(tokenKey);

            // 2. 清除用户缓存
            userCacheService.evictUserCache(username);

            // 3. 清除用户所有会话
            refreshTokenCacheService.revokeAllUserRefreshTokens(userId);

            // 4. 处理会话登出
            sessionManagementService.handleLogout(token, "USER_LOGOUT");
        }

        // 5. 将access token加入黑名单
        addTokenToBlacklist(token, "logout");

        log.info("用户登出成功 - 用户ID: {}, 用户名: {}", userId, username);
        return BizResult.success();
    }

    @Override
    public BizResult<LoginVO> refreshAccessToken(RefreshTokenContext refreshTokenContext) {
        long startTime = System.currentTimeMillis();
        String refreshToken = refreshTokenContext.getRefreshToken();

        // 1. 验证refresh token是否在缓存中且有效
        if (!refreshTokenCacheService.isValidRefreshToken(refreshToken)) {
            log.warn("RefreshToken缓存验证失败 - Token: {}",
                    refreshTokenCacheService.maskToken(refreshToken));
            return BizResult.fail(ErrorCode.Client.TOKEN_INVALID);
        }

        // 2. 验证令牌类型
        String tokenType = jwtValidator.extractTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            log.warn("非RefreshToken类型 - Token: {}, 类型: {}",
                    refreshTokenCacheService.maskToken(refreshToken), tokenType);
            return BizResult.fail(ErrorCode.Client.TOKEN_INVALID);
        }

        // 3. 提取用户信息
        String username = jwtValidator.extractUsername(refreshToken);
        Long userId = jwtValidator.extractUserId(refreshToken);
        if (username == null || userId == null) {
            log.warn("RefreshToken解析用户信息失败 - Token: {}",
                    refreshTokenCacheService.maskToken(refreshToken));
            return BizResult.fail(ErrorCode.Client.TOKEN_INVALID);
        }

        // 4. 获取旧access token
        String oldAccessToken = cacheUtil.getString(CacheKeys.Auth.accessToken(userId));
        
        // 5. 生成新令牌
        String newAccessToken = jwtGenerator.generateAccessToken(username, userId);
        String newRefreshToken = jwtGenerator.generateRefreshToken(username, userId);

        // 6. 获取旧token的元数据用于新token缓存
        RefreshTokenMetadata oldMetadata =
                refreshTokenCacheService.getMetadata(refreshToken);

        // 7. 更新refresh token缓存
        refreshTokenContext.setUserId(userId);
        refreshTokenContext.setRefreshToken(newRefreshToken);
        refreshTokenContext.setClientIp(ObjectUtil.isEmpty(oldMetadata) ? "unknown" : oldMetadata.getLoginIp());
        refreshTokenContext.setUserAgent(ObjectUtil.isEmpty(oldMetadata) ? "unknown" : oldMetadata.getUserAgent());
        refreshTokenCacheService.cacheRefreshToken(refreshTokenContext);

        // 8. 撤销旧的refresh token
        refreshTokenCacheService.revokeRefreshToken(refreshToken);

        // 9. 将旧的refresh token加入黑名单
        addTokenToBlacklist(refreshToken, "refreshed");

        // 10. 将旧的access token加入黑名单
        if (oldAccessToken != null) {
            addTokenToBlacklist(oldAccessToken, "refreshed");
        }

        refreshTokenContext.setNewRefreshToken(newRefreshToken);
        refreshTokenContext.setNewAccessToken(newAccessToken);

        // 11. 更新access token缓存
        cacheLoginToken(refreshTokenContext);

        // 12. 构建响应
        LoginVO response = buildTokenResponse(refreshTokenContext);

        long endTime = System.currentTimeMillis();
        log.info("Token刷新成功 - 用户: {}, 耗时: {}ms", username, (endTime - startTime));

        return BizResult.success(response);

    }

    // /**
    //  * 强制用户下线
    //  */
    // public void forceLogoutUser(Long userId, String reason) {
    //     try {
    //         // 1. 撤销用户所有refresh token
    //         refreshTokenCacheService.revokeAllUserRefreshTokens(userId);
    //
    //         // 2. 清除用户access token缓存
    //         String tokenKey = CacheKeys.buildLoginTokenKey(userId);
    //         stringRedisTemplate.delete(tokenKey);
    //
    //         // 3. 清除用户缓存
    //         userCacheService.evictUserCacheByUserId(userId);
    //
    //         log.info("用户被强制下线 - 用户ID: {}, 原因: {}", userId, reason);
    //     } catch (Exception e) {
    //         log.error("强制用户下线失败 - 用户ID: {}", userId, e);
    //     }
    // }

    /**
     * 检查token是否在黑名单中
     */
    private boolean isTokenBlacklisted(String token) {
        String blacklistKey = CacheKeys.Security.blacklistedToken(token);
        return cacheUtil.hasKeyString(blacklistKey);
    }

    /**
     * 添加token到黑名单
     */
    @Override
    public void addTokenToBlacklist(String token, String reason) {
        String blacklistKey = CacheKeys.Security.blacklistedToken(token);
        try {
            Date expiration = jwtValidator.extractExpiration(token);
            long expiresIn = expiration != null ?
                    (expiration.getTime() - System.currentTimeMillis()) :
                    cacheConfig.getExpire(blacklistKey, TimeUnit.SECONDS) * 1000;

            if (expiresIn > 0) {
                cacheUtil.putString(
                        blacklistKey,
                        reason,
                        expiresIn,
                        TimeUnit.MILLISECONDS
                );
                log.debug("Token加入黑名单 - Token: {}, 原因: {}, 过期时间: {}ms",
                        refreshTokenCacheService.maskToken(token), reason, expiresIn);
            }
        } catch (Exception e) {
            log.error("添加Token到黑名单失败 - Token: {}",
                    refreshTokenCacheService.maskToken(token), e);
        }
    }

    /**
     * 缓存登录token
     */
    private void cacheLoginToken(RefreshTokenContext refreshTokenContext) {
        Long userId = refreshTokenContext.getUserId();
        String newAccessToken = refreshTokenContext.getNewAccessToken();
        try {
            String tokenKey = CacheKeys.Auth.accessToken(userId);
            cacheUtil.putStringSmart(tokenKey, newAccessToken);
        } catch (Exception e) {
            log.error("缓存登录Token失败 - 用户ID: {}", userId, e);
        }
    }

    /**
     * 构建令牌响应
     */
    private LoginVO buildTokenResponse(RefreshTokenContext refreshTokenContext) {
        LoginVO response = new LoginVO();
        response.setAccessToken(refreshTokenContext.getNewAccessToken());
        response.setTokenType(TokenTypeEnum.BEARER.getType());
        response.setExpiresIn(cacheUtil.getExpire(
                        CacheKeys.Auth.accessToken(refreshTokenContext.getUserId()))
        );
        response.setRefreshToken(refreshTokenContext.getNewRefreshToken());
        response.setRefreshExpiresIn(cacheUtil.getExpire(
                CacheKeys.Auth.refreshToken(refreshTokenContext.getNewRefreshToken())
        ));
        return response;
    }
}