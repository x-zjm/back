package com.nianji.auth.service;

import com.nianji.auth.entity.User;

/**
 * 用户缓存服务接口
 * 负责用户相关数据的缓存管理，提高系统性能
 */
public interface UserCacheService {

    /**
     * 刷新用户缓存
     *
     * @param user 用户信息
     */
    void refreshUserCache(User user);

    /**
     * 清除用户缓存
     *
     * @param username 用户名
     */
    void evictUserCache(String username);

    /**
     * 清除用户所有活跃会话
     *
     * @param userId 用户ID
     */
    void clearAllUserSessions(Long userId);
}