package com.nianji.auth.service.impl;

import com.nianji.auth.config.AuthSecurityConfig;
import com.nianji.auth.context.LoginContext;
import com.nianji.auth.service.LoginSecurityService;
import com.nianji.auth.filter.UserBloomFilterService;
import com.nianji.common.constant.CacheKeys;
import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.reqres.BizResult;
import com.nianji.common.utils.CacheUtil;
import com.nianji.common.utils.CommonUtil;
import com.nianji.common.utils.IpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录安全防护服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginSecurityServiceImpl implements LoginSecurityService {

    private final UserBloomFilterService userBloomFilterService;

    private final CacheUtil cacheUtil;

    private AuthSecurityConfig authSecurityConfig;

    @Override
    public BizResult<Void> checkLoginSecurity(LoginContext loginContext) {
        String clientIp = loginContext.getClientIp();
        String username = loginContext.getUsername();
        // 1. IP 地址验证
        if (!IpUtil.isValidIp(clientIp)) {
            log.warn("无效的 IP 地址尝试登录: {}", clientIp);
            loginContext.setFailReason("无效的客户端地址");
            recordFailedAttempt(loginContext);
            return BizResult.fail(ErrorCode.Client.INVALID_CREDENTIALS);
        }

        // 2. 布隆过滤器快速判断
        if (!userBloomFilterService.mightUsernameExist(username)) {
            loginContext.setFailReason("用户不存在（布隆过滤器快速判断）");
            recordFailedAttempt(loginContext);
            return BizResult.fail(ErrorCode.Client.INVALID_CREDENTIALS);
        }

        // 3. 用户账户锁定检查
        String userLockKey = CacheKeys.Auth.userLock(username);
        if (cacheUtil.hasKeyString(userLockKey)) {
            Long ttl = cacheUtil.getExpireString(userLockKey, TimeUnit.MINUTES);
            return BizResult.fail(ErrorCode.Client.ACCOUNT_LOCKED,
                    // String.format("账户已被锁定，请在%d分钟后重试", ttl != null ? ttl : lockDurationMinutes));
                    "账户暂时被锁定，请稍后重试");
        }

        // 4. IP锁定检查
        String ipLockKey = CacheKeys.Auth.ipLock(clientIp);
        if (cacheUtil.hasKeyString(ipLockKey)) {
            Long ttl = cacheUtil.getExpireString(ipLockKey, TimeUnit.MINUTES);
            return BizResult.fail(ErrorCode.Client.IP_LOCKED,
                    // String.format("IP已被锁定，请在%d分钟后重试", ttl != null ? ttl : ipLockDurationMinutes)););
                    "账户暂时被锁定，请稍后重试");
        }

        // 5. 失败次数检查
        BizResult<Void> attemptsCheck = checkLoginAttempts(username, clientIp);
        if (!attemptsCheck.isSuccess()) {
            return attemptsCheck;
        }

        return BizResult.success();
    }

    private BizResult<Void> checkLoginAttempts(String username, String loginIp) {
        // 检查用户失败次数
        String userAttemptsKey = CacheKeys.Auth.loginAttemptsByUser(username);
        String userAttemptsStr = cacheUtil.getString(userAttemptsKey);
        Long userAttempts = CommonUtil.convertToLong(userAttemptsStr);
        if (userAttempts != null && userAttempts >= authSecurityConfig.getMaxLoginAttempts()) {
            lockAccount(username);
            return BizResult.fail(ErrorCode.Client.USER_RATE_LIMIT,
                    ErrorCode.Client.RATE_LIMIT_EXCEEDED.getMessage());
        }

        // 检查IP失败次数
        String ipAttemptsKey = CacheKeys.Auth.loginAttemptsByIp(loginIp);
        String ipAttemptsStr = cacheUtil.getString(ipAttemptsKey);
        Long ipAttempts = CommonUtil.convertToLong(ipAttemptsStr);
        if (ipAttempts != null && ipAttempts >= authSecurityConfig.getIpMaxAttempts()) {
            lockIp(loginIp);
            return BizResult.fail(ErrorCode.Client.IP_RATE_LIMIT,
                    ErrorCode.Client.RATE_LIMIT_EXCEEDED.getMessage());
        }

        return BizResult.success();
    }

    @Override
    public void recordFailedAttempt(LoginContext loginContext) {
        String username = loginContext.getUsername();
        String clientIp = loginContext.getClientIp();
        String failReason = loginContext.getFailReason();

        try {
            // 记录用户失败次数
            String userAttemptsKey = CacheKeys.Auth.loginAttemptsByUser(username);
            Long userAttempts = cacheUtil.incrementString(userAttemptsKey, 1);
            cacheUtil.expireString(userAttemptsKey, authSecurityConfig.getLockDurationMinutes() * 60L, TimeUnit.SECONDS);

            // 记录IP失败次数
            String ipAttemptsKey = CacheKeys.Auth.loginAttemptsByIp(clientIp);
            Long ipAttempts = cacheUtil.incrementString(ipAttemptsKey, 1);
            cacheUtil.expireString(ipAttemptsKey, authSecurityConfig.getIpLockDurationMinutes() * 60L, TimeUnit.SECONDS);

            log.debug("登录失败记录 - 用户: {}, IP: {}, 原因: {}, 用户失败次数: {}, IP失败次数: {}",
                    username, IpUtil.anonymizeIp(clientIp), failReason, userAttempts, ipAttempts);
        } catch (Exception e) {
            log.error("记录登录失败次数失败", e);
        }
    }

    @Override
    public void checkAndLockAccount(String username, String loginIp) {
        try {
            String userAttemptsKey = CacheKeys.Auth.loginAttemptsByUser(username);
            String attemptsStr = cacheUtil.getString(userAttemptsKey);
            Long attempts = CommonUtil.convertToLong(attemptsStr);
            if (attempts != null && attempts >= authSecurityConfig.getMaxLoginAttempts() - 1) {
                lockAccount(username);
                log.warn("用户账户已被锁定: {}, IP: {}", username, IpUtil.anonymizeIp(loginIp));
            }
        } catch (Exception e) {
            log.error("检查锁定账户失败", e);
        }
    }

    private void lockAccount(String username) {
        String lockKey = CacheKeys.Auth.userLock(username);
        cacheUtil.putString(
                lockKey, "locked", authSecurityConfig.getLockDurationMinutes(), TimeUnit.MINUTES
        );
        // 清除失败计数
        String attemptsKey = CacheKeys.Auth.loginAttemptsByUser(username);
        cacheUtil.deleteString(attemptsKey);
        log.warn("用户账户锁定: {}", username);
    }

    private void lockIp(String loginIp) {
        String lockKey = CacheKeys.Auth.ipLock(loginIp);
        cacheUtil.putString(
                lockKey, "locked", authSecurityConfig.getIpLockDurationMinutes(), TimeUnit.MINUTES
        );
        // 清除IP失败计数
        String attemptsKey = CacheKeys.Auth.loginAttemptsByIp(loginIp);
        cacheUtil.deleteString(attemptsKey);
        log.warn("IP锁定: {}", IpUtil.anonymizeIp(loginIp));
    }

    @Override
    public void clearFailedAttempts(LoginContext loginContext) {
        try {
            String userAttemptsKey = CacheKeys.Auth.loginAttemptsByUser(loginContext.getUsername());
            String ipAttemptsKey = CacheKeys.Auth.loginAttemptsByIp(loginContext.getClientIp());
            cacheUtil.deleteString(userAttemptsKey);
            cacheUtil.deleteString(ipAttemptsKey);
        } catch (Exception e) {
            log.error("清除失败计数失败", e);
        }
    }

}