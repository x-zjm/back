package com.nianji.auth.service.impl;

import com.nianji.auth.entity.User;
import com.nianji.auth.service.UserCacheService;
import com.nianji.common.constant.CacheKeys;
import com.nianji.common.utils.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 用户缓存服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCacheServiceImpl implements UserCacheService {

    private final CacheUtil cacheUtil;

    @Override
    public void refreshUserCache(User user) {
        String cacheKey = CacheKeys.User.infoByUsername(user.getUsername());
        cacheUtil.putSmart(cacheKey, user);
    }

    @Override
    public void evictUserCache(String username) {
        try {
            String cacheKey = CacheKeys.User.infoByUsername(username);
            cacheUtil.delete(cacheKey);
        } catch (Exception e) {
            log.error("清除用户缓存失败: {}", username, e);
        }
    }

    @Override
    public void clearAllUserSessions(Long userId) {
        try {
            String tokenKey = CacheKeys.Auth.accessToken(userId);
            cacheUtil.deleteString(tokenKey);
        } catch (Exception e) {
            log.error("清除用户会话失败: {}", userId, e);
        }
    }
}