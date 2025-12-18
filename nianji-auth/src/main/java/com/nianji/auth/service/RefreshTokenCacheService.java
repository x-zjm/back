package com.nianji.auth.service;

import com.nianji.auth.context.RefreshTokenContext;
import com.nianji.auth.model.device.DeviceInfo;
import com.nianji.auth.model.token.RefreshTokenMetadata;

import java.util.List;

/**
 * RefreshToken缓存服务 - 专注缓存操作
 */
public interface RefreshTokenCacheService {

    /**
     * 缓存RefreshToken
     *
     * @param refreshTokenContext 刷新令牌上下文
     */
    void cacheRefreshToken(RefreshTokenContext refreshTokenContext);

    /**
     * 验证RefreshToken是否有效
     *
     * @param refreshToken 刷新令牌
     * @return 是否有效
     */
    boolean isValidRefreshToken(String refreshToken);

    /**
     * 获取RefreshToken元数据
     *
     * @param refreshToken 刷新令牌
     * @return 元数据信息
     */
    RefreshTokenMetadata getMetadata(String refreshToken);

    /**
     * 撤销RefreshToken
     *
     * @param refreshToken 刷新令牌
     */
    void revokeRefreshToken(String refreshToken);

    /**
     * 撤销用户所有RefreshToken
     *
     * @param userId 用户ID
     */
    void revokeAllUserRefreshTokens(Long userId);

    /**
     * 撤销除指定令牌外的所有会话
     *
     * @param userId 用户ID
     * @param excludeToken 排除的令牌
     */
    void revokeUserSessionsExcept(Long userId, String excludeToken);

    /**
     * 获取用户活跃会话
     *
     * @param userId 用户ID
     * @return 活跃会话列表
     */
    List<RefreshTokenMetadata> getUserActiveSessions(Long userId);

    /**
     * 更新最后使用时间
     *
     * @param refreshToken 刷新令牌
     */
    void updateLastUsedTime(String refreshToken);

    /**
     * 记录登录设备信息
     *
     * @param userId 用户ID
     * @param loginIp 登录IP
     * @param userAgent 用户代理
     */
    void recordLoginDevice(Long userId, String loginIp, String userAgent);

    /**
     * 获取最近登录设备
     *
     * @param userId 用户ID
     * @return 设备信息列表
     */
    List<DeviceInfo> getRecentLoginDevices(Long userId);

    /**
     * 掩码令牌（用于日志记录）
     *
     * @param token 原始令牌
     * @return 掩码后的令牌
     */
    String maskToken(String token);
}