package com.nianji.auth.service.impl;

import com.nianji.auth.context.RefreshTokenContext;
import com.nianji.auth.model.device.DeviceInfo;
import com.nianji.auth.model.token.RefreshTokenMetadata;
import com.nianji.auth.service.RefreshTokenCacheService;
import com.nianji.common.config.CacheConfig;
import com.nianji.common.constant.CacheKeys;
import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.ExceptionFactory;
import com.nianji.common.utils.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * RefreshToken缓存服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenCacheServiceImpl implements RefreshTokenCacheService {

    private final CacheUtil cacheUtil;
    private final CacheConfig cacheConfig;

    @Override
    public void cacheRefreshToken(RefreshTokenContext refreshTokenContext) {
        Long userId = refreshTokenContext.getUserId();
        String clientIp = refreshTokenContext.getClientIp();
        String refreshToken = refreshTokenContext.getRefreshToken();
        String userAgent = refreshTokenContext.getUserAgent();

        try {
            // 1. 缓存refreshToken元数据
            String tokenKey = CacheKeys.Auth.refreshToken(refreshToken);
            RefreshTokenMetadata metadata = new RefreshTokenMetadata(userId, clientIp, userAgent);

            cacheUtil.putSmart(tokenKey, metadata);

            // 2. 维护用户会话列表
            String userSessionsKey = CacheKeys.Auth.userSessions(userId);
            cacheUtil.addSmart(userSessionsKey, refreshToken);

            // 3. 记录登录设备信息
            recordLoginDevice(userId, clientIp, userAgent);

            log.debug("RefreshToken缓存成功 - 用户ID: {}, Token: {}", userId, maskToken(refreshToken));
        } catch (Exception e) {
            log.error("缓存RefreshToken失败 - 用户ID: {}", userId, e);
            throw ExceptionFactory.business(ErrorCode.Business.BUSINESS_ERROR,
                    "刷新令牌失败");
        }
    }

    @Override
    public boolean isValidRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return false;
        }

        try {
            RefreshTokenMetadata metadata = getMetadata(refreshToken);

            boolean isValid = metadata != null && !metadata.isRevoked();
            log.debug("RefreshToken验证 - Token: {}, 有效: {}", maskToken(refreshToken), isValid);

            return isValid;
        } catch (Exception e) {
            log.error("验证RefreshToken失败 - Token: {}", maskToken(refreshToken), e);
            return false;
        }
    }

    @Override
    public RefreshTokenMetadata getMetadata(String refreshToken) {
        try {
            String tokenKey = CacheKeys.Auth.refreshToken(refreshToken);
            return cacheUtil.get(tokenKey);
        } catch (Exception e) {
            log.error("获取RefreshToken元数据失败 - Token: {}", maskToken(refreshToken), e);
            return null;
        }
    }

    @Override
    public void revokeRefreshToken(String refreshToken) {
        try {
            RefreshTokenMetadata metadata = getMetadata(refreshToken);
            if (metadata != null) {
                // 标记为已撤销，短期保留记录用于审计
                metadata.setRevoked(true);
                String tokenKey = CacheKeys.Auth.refreshToken(refreshToken);
                cacheUtil.put(
                        tokenKey,
                        metadata,
                        60,
                        TimeUnit.MINUTES
                );

                // 从用户会话列表中移除
                String userSessionsKey = CacheKeys.Auth.userSessions(metadata.getUserId());
                cacheUtil.remove(userSessionsKey, refreshToken);

                log.info("RefreshToken已撤销 - 用户ID: {}, Token: {}",
                        metadata.getUserId(), maskToken(refreshToken));
            }
        } catch (Exception e) {
            log.error("撤销RefreshToken失败 - Token: {}", maskToken(refreshToken), e);
        }
    }

    @Override
    public void revokeAllUserRefreshTokens(Long userId) {
        try {
            String userSessionsKey = CacheKeys.Auth.userSessions(userId);
            Set<Object> refreshTokens = cacheUtil.members(userSessionsKey);

            if (refreshTokens != null) {
                for (Object tokenObj : refreshTokens) {
                    String refreshToken = (String) tokenObj;
                    revokeRefreshToken(refreshToken);
                }
                log.info("用户所有RefreshToken已撤销 - 用户ID: {}, 撤销数量: {}", userId, refreshTokens.size());
            }
        } catch (Exception e) {
            log.error("撤销用户所有RefreshToken失败 - 用户ID: {}", userId, e);
        }
    }

    @Override
    public void revokeUserSessionsExcept(Long userId, String excludeToken) {
        try {
            String userSessionsKey = CacheKeys.Auth.userSessions(userId);
            Set<Object> refreshTokens = cacheUtil.members(userSessionsKey);

            if (refreshTokens != null) {
                int revokedCount = 0;
                for (Object tokenObj : refreshTokens) {
                    String refreshToken = (String) tokenObj;
                    if (!refreshToken.equals(excludeToken)) {
                        revokeRefreshToken(refreshToken);
                        revokedCount++;
                    }
                }
                log.info("用户其他会话已撤销 - 用户ID: {}, 保留Token: {}, 撤销数量: {}",
                        userId, maskToken(excludeToken), revokedCount);
            }
        } catch (Exception e) {
            log.error("撤销用户其他会话失败 - 用户ID: {}", userId, e);
        }
    }

    @Override
    public List<RefreshTokenMetadata> getUserActiveSessions(Long userId) {
        try {
            String userSessionsKey = CacheKeys.Auth.userSessions(userId);
            Set<Object> refreshTokens = cacheUtil.members(userSessionsKey);

            if (refreshTokens == null) {
                return List.of();
            }

            return refreshTokens.stream()
                    .map(token -> getMetadata((String) token))
                    .filter(metadata -> metadata != null && !metadata.isRevoked())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取用户活跃会话失败 - 用户ID: {}", userId, e);
            return List.of();
        }
    }

    @Override
    public void updateLastUsedTime(String refreshToken) {
        try {
            RefreshTokenMetadata metadata = getMetadata(refreshToken);
            if (metadata != null && !metadata.isRevoked()) {
                metadata.setLastUsedAt(LocalDateTime.now());
                String tokenKey = CacheKeys.Auth.refreshToken(refreshToken);
                cacheUtil.putSmart(tokenKey, metadata);
            }
        } catch (Exception e) {
            log.error("更新RefreshToken最后使用时间失败 - Token: {}", maskToken(refreshToken), e);
        }
    }

    @Override
    public void recordLoginDevice(Long userId, String loginIp, String userAgent) {
        try {
            // 统一使用 Auth 模块的设备键
            String deviceKey = CacheKeys.Auth.trustedDevices(userId);
            DeviceInfo deviceInfo = new DeviceInfo(loginIp, userAgent, LocalDateTime.now());

            // 存储最近5个登录设备
            cacheUtil.leftPush(deviceKey, deviceInfo);
            cacheUtil.trim(deviceKey, 0, 4);
            cacheUtil.expire(deviceKey, cacheConfig.getExpire(deviceKey), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("记录登录设备信息失败 - 用户ID: {}", userId, e);
        }
    }

    @Override
    public List<DeviceInfo> getRecentLoginDevices(Long userId) {
        try {
            // 统一使用 Auth 模块的设备键
            String deviceKey = CacheKeys.Auth.trustedDevices(userId);
            List<Object> devices = cacheUtil.range(deviceKey, 0, -1);

            if (devices == null) {
                return List.of();
            }

            return devices.stream()
                    .map(device -> (DeviceInfo) device)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取用户登录设备失败 - 用户ID: {}", userId, e);
            return List.of();
        }
    }

    @Override
    public String maskToken(String token) {
        if (token == null || token.length() <= 16) {
            return "***";
        }
        return token.substring(0, 8) + "..." + token.substring(token.length() - 8);
    }
}