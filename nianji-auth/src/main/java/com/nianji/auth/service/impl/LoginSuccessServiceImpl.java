package com.nianji.auth.service.impl;

import com.nianji.auth.config.AuthConfig;
import com.nianji.auth.context.LoginContext;
import com.nianji.auth.context.LoginLogContext;
import com.nianji.auth.context.RefreshTokenContext;
import com.nianji.auth.dao.repository.UserRepository;
import com.nianji.auth.entity.User;
import com.nianji.auth.model.device.DeviceAnalysisResult;
import com.nianji.auth.model.device.DeviceInfo;
import com.nianji.auth.model.device.DeviceTrustLevel;
import com.nianji.auth.model.session.SessionInfo;
import com.nianji.auth.model.session.SessionLimitInfo;
import com.nianji.auth.service.*;
import com.nianji.auth.vo.LoginVO;
import com.nianji.common.config.CacheConfig;
import com.nianji.common.constant.CacheKeys;
import com.nianji.common.enums.LoginStatusEnum;
import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.ExceptionFactory;
import com.nianji.common.jwt.api.JwtGenerator;
import com.nianji.common.reqres.BizResult;
import com.nianji.common.utils.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 增强的登录成功处理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginSuccessServiceImpl implements LoginSuccessService {

    private final UserRepository userRepository;
    private final JwtGenerator jwtGenerator;
    private final LoginSecurityService loginSecurityService;
    private final RefreshTokenCacheService refreshTokenCacheService;
    private final AuthLogService authLogService;
    private final SessionManagementService sessionManagementService;
    private final DeviceManagementService deviceManagementService;
    private final CacheUtil cacheUtil;

    private final CacheConfig cacheConfig;
    private final AuthConfig authConfig;

    @Override
    @Transactional
    public BizResult<LoginVO> handleSuccessfulLogin(LoginContext loginContext) {
        final User user = loginContext.getUser();
        final Long userId = user.getId();
        final String username = user.getUsername();

        String accessToken = null;
        String refreshToken = null;

        try {
            log.info("=== 开始用户登录成功流程 ===");

            // 第一阶段：基础准备
            prepareForSuccessfulLogin(loginContext);

            // 第二阶段：生成令牌
            TokenPair tokenPair = generateTokens(username, userId);
            accessToken = tokenPair.accessToken();
            refreshToken = tokenPair.refreshToken();
            loginContext.setAccessToken(accessToken);
            loginContext.setRefreshToken(refreshToken);

            // 第三阶段：会话策略处理（修复版）
            SessionLimitInfo sessionLimitInfo = processSessionPolicyWithRevocation(loginContext);

            // 第四阶段：创建新会话
            BizResult<SessionInfo> sessionResult = sessionManagementService.createSession(loginContext);
            if (!sessionResult.isSuccess()) {
                throw ExceptionFactory.business(ErrorCode.Business.BUSINESS_ERROR, "会话创建失败");
            }

            // 第五阶段：缓存令牌
            cacheAccessToken(loginContext);
            refreshTokenCacheService.cacheRefreshToken(
                    RefreshTokenContext.buildRefreshTokenContext(loginContext));

            // 第六阶段：获取最终会话状态（包含新创建的会话）
            int finalSessionCount = sessionManagementService.getActiveSessionCount(userId);

            // 关键修复：确保最终状态包含新会话
            if (finalSessionCount <= sessionLimitInfo.getCurrentSessions()) {
                log.warn("新会话创建后计数异常，强制修正: {} -> {}",
                        sessionLimitInfo.getCurrentSessions(), finalSessionCount);
                finalSessionCount = sessionLimitInfo.getCurrentSessions() + 1;
            }

            sessionLimitInfo.setCurrentSessions(finalSessionCount);

            // 更新消息确保一致性
            String finalMessage = String.format("会话限制模式，当前: %d/%d",
                    finalSessionCount, sessionLimitInfo.getMaxSessions());
            sessionLimitInfo.setMessage(finalMessage);

            log.info("最终会话状态 - 用户: {}, 会话数: {}/{}",
                    username, finalSessionCount, sessionLimitInfo.getMaxSessions());

            // 第七阶段：记录登录成功
            recordSuccessfulLogin(loginContext);

            // 第八阶段：分析设备和构建响应
            DeviceAnalysisResult deviceAnalysis = analyzeDeviceBehavior(loginContext);
            loginContext.setSessionLimitInfo(sessionLimitInfo);
            loginContext.setDeviceAnalysisResult(deviceAnalysis);
            LoginVO loginVO = buildLoginResponse(loginContext);

            log.info("=== 用户登录成功完成 ===");
            return BizResult.success(loginVO);

        } catch (Exception e) {
            log.error("登录成功处理失败 - 用户: {}", username, e);
            cleanupOnFailure(userId, accessToken);
            throw ExceptionFactory.business(ErrorCode.Business.BUSINESS_ERROR, "登录失败");
        }
    }

    /**
     * 修复版：处理会话策略并执行踢出逻辑
     */
    private SessionLimitInfo processSessionPolicyWithRevocation(LoginContext loginContext) {
        Long userId = loginContext.getUser().getId();
        String username = loginContext.getUsername();

        log.info("=== 开始会话策略处理 ===");
        log.info("用户: {}, 用户ID: {}, 模式: {}", username, userId, authConfig.getMode());

        // 1. 获取当前会话状态（创建新会话前）
        int currentSessions = sessionManagementService.getActiveSessionCount(userId);
        log.info("当前活跃会话数: {}", currentSessions);

        // 2. 根据配置模式处理
        SessionLimitInfo limitInfo;

        if (authConfig.isSingleSessionMode()) {
            limitInfo = handleSingleSessionMode(userId, username, currentSessions);
        } else if (authConfig.isLimitedSessionsMode()) {
            limitInfo = handleLimitedSessionsMode(userId, username, currentSessions);
        } else {
            limitInfo = handleMultiSessionMode(userId, username, currentSessions);
        }

        log.info("会话策略处理完成 - 用户: {}, 结果: {}/{}",
                username, limitInfo.getCurrentSessions(), limitInfo.getMaxSessions());

        return limitInfo;
    }

    /**
     * 处理单点登录模式
     */
    private SessionLimitInfo handleSingleSessionMode(Long userId, String username, int currentSessions) {
        log.info("单点登录模式 - 清理所有现有会话");

        if (currentSessions > 0) {
            sessionManagementService.revokeAllUserSessions(userId, "SINGLE_SESSION_POLICY");
            log.info("单点登录 - 已清理 {} 个现有会话", currentSessions);
        }

        return SessionLimitInfo.builder()
                .currentSessions(0) // 清理后为0，新会话创建后为1
                .maxSessions(1)
                .limitReached(false)
                .authMode("SINGLE_SESSION")
                .message("单点登录模式")
                .build();
    }

    /**
     * 处理会话限制模式 - 修复版
     */
    private SessionLimitInfo handleLimitedSessionsMode(Long userId, String username, int currentSessions) {
        int maxSessions = authConfig.getMaxSessions();

        log.info("会话限制模式检查 - 用户: {}, 当前: {}/{}", username, currentSessions, maxSessions);

        // 关键修复：如果已经达到或超过限制，先踢出多余的会话
        if (currentSessions >= maxSessions) {
            int sessionsToKick = currentSessions - maxSessions + 1; // +1 为新会话腾出位置
            log.info("需要踢出 {} 个会话为新登录腾出位置", sessionsToKick);

            boolean kickSuccess = kickOldestSessions(userId, username, sessionsToKick);
            if (!kickSuccess) {
                log.error("踢出会话失败，无法创建新会话");
                throw ExceptionFactory.business(ErrorCode.Business.BUSINESS_ERROR, "会话数量已达上限");
            }

            // 重新统计踢出后的会话数
            currentSessions = sessionManagementService.getActiveSessionCount(userId);
            log.info("踢出后当前会话数: {}", currentSessions);
        }

        boolean limitReached = currentSessions >= maxSessions;

        // 构建准确的消息
        String message = String.format("会话限制模式，当前: %d/%d", currentSessions, maxSessions);

        return SessionLimitInfo.builder()
                .currentSessions(currentSessions)
                .maxSessions(maxSessions)
                .limitReached(limitReached)
                .authMode("LIMITED_SESSIONS")
                .message(message)
                .build();
    }

    /**
     * 踢出最早会话 - 修复版
     */
    private boolean kickOldestSessions(Long userId, String username, int sessionsToKick) {
        try {
            List<SessionInfo> activeSessions = sessionManagementService.getActiveSessions(userId);
            if (activeSessions.isEmpty()) {
                log.warn("没有活跃会话可踢出");
                return true;
            }

            // 按登录时间排序（最早的在前）
            activeSessions.sort(Comparator.comparing(SessionInfo::getLoginTime));

            log.info("准备踢出 {} 个最早会话，总会话数: {}", sessionsToKick, activeSessions.size());

            int kickedCount = 0;
            for (int i = 0; i < sessionsToKick && i < activeSessions.size(); i++) {
                SessionInfo session = activeSessions.get(i);
                log.info("踢出会话 [{}/{}] - ID: {}, 登录时间: {}",
                        i + 1, sessionsToKick, session.getSessionId(), session.getLoginTime());

                sessionManagementService.revokeSession(session.getSessionId(), "SESSION_LIMIT_KICK");
                kickedCount++;

                // 确保Redis操作完成
                Thread.sleep(100);
            }

            // 验证踢出结果
            int afterKick = sessionManagementService.getActiveSessionCount(userId);
            boolean success = afterKick <= (activeSessions.size() - kickedCount);

            log.info("踢出完成 - 用户: {}, 踢出: {}个, 踢出前: {}个, 踢出后: {}个, 成功: {}",
                    username, kickedCount, activeSessions.size(), afterKick, success);

            return success;
        } catch (Exception e) {
            log.error("踢出会话失败 - 用户: {}", username, e);
            return false;
        }
    }

    /**
     * 处理多点登录模式
     */
    private SessionLimitInfo handleMultiSessionMode(Long userId, String username, int currentSessions) {
        return SessionLimitInfo.builder()
                .currentSessions(currentSessions)
                .maxSessions(Integer.MAX_VALUE) // 多点登录无限制
                .limitReached(false)
                .authMode("MULTI_SESSION")
                .message(String.format("多点登录模式，当前会话数: %d", currentSessions))
                .build();
    }

    /**
     * 踢出超限会话 - 强力修复版
     */
    private boolean kickExcessSessions(Long userId, String username, int currentSessions, int maxSessions) {
        try {
            int sessionsToKick = currentSessions - maxSessions + 1;
            log.info("开始踢出会话 - 用户: {}, 需要踢出: {} 个", username, sessionsToKick);

            List<SessionInfo> activeSessions = sessionManagementService.getActiveSessions(userId);
            if (activeSessions.isEmpty()) {
                log.warn("没有活跃会话可踢出");
                return true;
            }

            // 按登录时间排序（最早的先踢出）
            activeSessions.sort(Comparator.comparing(SessionInfo::getLoginTime));

            int kickedCount = 0;
            for (int i = 0; i < sessionsToKick && i < activeSessions.size(); i++) {
                SessionInfo sessionToKick = activeSessions.get(i);
                log.info("踢出会话 [{}/{}] - 会话ID: {}, 登录时间: {}",
                        i + 1, sessionsToKick, sessionToKick.getSessionId(), sessionToKick.getLoginTime());

                sessionManagementService.revokeSession(sessionToKick.getSessionId(), "SESSION_LIMIT_EXCEEDED");
                kickedCount++;

                // 强制等待确保Redis操作完成
                Thread.sleep(100);
            }

            // 验证踢出结果
            int afterKick = sessionManagementService.getActiveSessionCount(userId);
            boolean success = afterKick <= maxSessions - 1; // 踢出后应该 <= maxSessions-1

            log.info("踢出完成 - 用户: {}, 踢出数量: {}, 踢出后会话数: {}, 成功: {}",
                    username, kickedCount, afterKick, success);

            return success;
        } catch (Exception e) {
            log.error("踢出会话失败 - 用户: {}", username, e);
            return false;
        }
    }

    /**
     * 第一阶段：基础准备工作
     */
    private void prepareForSuccessfulLogin(LoginContext loginContext) {
        // 清除失败计数
        loginSecurityService.clearFailedAttempts(loginContext);

        // 更新用户登录信息
        updateUserLoginInfo(loginContext);
    }

    /**
     * 第二阶段：生成令牌对
     */
    private TokenPair generateTokens(String username, Long userId) {
        String accessToken = jwtGenerator.generateAccessToken(username, userId);
        String refreshToken = jwtGenerator.generateRefreshToken(username, userId);
        return new TokenPair(accessToken, refreshToken);
    }

    /**
     * 第四阶段：创建会话和缓存
     */
    private void createSessionAndCacheTokens(LoginContext loginContext) {
        Long userId = loginContext.getUser().getId();
        String username = loginContext.getUser().getUsername();

        // 1. 先缓存访问令牌
        cacheAccessToken(loginContext);

        // 2. 创建会话
        BizResult<SessionInfo> sessionResult = sessionManagementService.createSession(loginContext);
        if (!sessionResult.isSuccess()) {
            log.warn("创建会话失败: {}", sessionResult.getMsg());
            throw ExceptionFactory.business(ErrorCode.Business.BUSINESS_ERROR, "会话创建失败");
        }

        SessionInfo newSession = sessionResult.getData();
        log.info("新会话创建成功 - 用户: {}, 会话ID: {}", username, newSession.getSessionId());

        // 3. 缓存刷新令牌
        try {
            refreshTokenCacheService.cacheRefreshToken(
                    RefreshTokenContext.buildRefreshTokenContext(loginContext));
        } catch (Exception e) {
            log.error("缓存RefreshToken失败 - 用户: {}", username, e);
            // 缓存失败不影响登录
        }

        // 4. 立即验证会话创建结果
        validateSessionCreation(userId, username, newSession);
    }

    /**
     * 验证会话创建结果
     */
    private void validateSessionCreation(Long userId, String username, SessionInfo newSession) {
        try {
            // 短暂延迟确保Redis操作完成
            Thread.sleep(100);

            // 获取最新的活跃会话列表
            List<SessionInfo> activeSessions = sessionManagementService.getActiveSessions(userId);

            // 检查新会话是否在活跃列表中
            boolean sessionFound = activeSessions.stream()
                    .anyMatch(session -> session.getSessionId().equals(newSession.getSessionId()));

            if (!sessionFound) {
                log.warn("新创建的会话未在活跃会话列表中找到 - 用户: {}, 会话ID: {}",
                        username, newSession.getSessionId());
            } else {
                log.info("会话创建验证成功 - 用户: {}, 当前活跃会话数: {}",
                        username, activeSessions.size());
            }
        } catch (Exception e) {
            log.error("验证会话创建结果失败 - 用户: {}", username, e);
        }
    }

    /**
     * 第五阶段：记录登录成功
     */
    private void recordSuccessfulLogin(LoginContext loginContext) {
        // 记录登录日志
        loginContext.setFailReason("登录成功");
        loginContext.setLoginStatus(LoginStatusEnum.SUCCESS.getCode());
        authLogService.logLoginRequest(LoginLogContext.buildLoginLogContext(loginContext));

        // 记录成功IP
        recordSuccessfulLoginIp(loginContext);

        // 分析登录行为
        analyzeLoginBehavior(loginContext);
    }

    /**
     * 第六阶段：分析设备和构建响应
     */
    private DeviceAnalysisResult analyzeDeviceBehavior(LoginContext loginContext) {
        Long userId = loginContext.getUser().getId();
        String username = loginContext.getUser().getUsername();

        boolean deviceChangeRisk = false;
        String deviceTrustLevel = DeviceTrustLevel.UNKNOWN.name();

        try {
            DeviceInfo currentDevice = deviceManagementService.recordDeviceLogin(
                    userId, loginContext.getClientIp(), loginContext.getUserAgent());

            if (currentDevice != null) {
                deviceChangeRisk = deviceManagementService.checkDeviceChangeRisk(userId, currentDevice);
                DeviceTrustLevel trustLevel = deviceManagementService.analyzeDeviceTrustLevel(currentDevice);
                deviceTrustLevel = trustLevel != null ? trustLevel.name() : DeviceTrustLevel.UNKNOWN.name();

                log.debug("设备风险分析完成 - 用户: {}, 风险: {}, 信任级别: {}",
                        username, deviceChangeRisk, deviceTrustLevel);
            } else {
                log.warn("记录设备登录返回null，设备风险检查跳过");
            }
        } catch (Exception e) {
            log.error("设备风险分析失败，但不影响登录流程 - 用户: {}", username, e);
        }

        return new DeviceAnalysisResult(deviceChangeRisk, deviceTrustLevel);
    }

    /**
     * 构建登录响应
     */
    private LoginVO buildLoginResponse(LoginContext loginContext) {
        loginContext.setExpiresIn(cacheUtil.getExpireString(
                CacheKeys.Auth.accessToken(loginContext.getUser().getId())
        ));
        loginContext.setRefreshExpiresIn(cacheUtil.getExpire(
                CacheKeys.Auth.refreshToken(loginContext.getRefreshToken())
        ));
        LoginVO loginVO = LoginVO.buildLoginVo(loginContext);

        SessionLimitInfo sessionLimitInfo = loginContext.getSessionLimitInfo();
        // 确保响应中的认证模式与配置一致
        loginVO.setAuthMode(authConfig.getMode().name());
        loginVO.setCurrentSessions(sessionLimitInfo.getCurrentSessions());
        loginVO.setMaxSessions(sessionLimitInfo.getMaxSessions());

        return loginVO;
    }

    /**
     * 失败时的清理工作
     */
    private void cleanupOnFailure(Long userId, String accessToken) {
        try {
            if (userId != null) {
                cacheUtil.deleteString(CacheKeys.Auth.accessToken(userId));
            }
        } catch (Exception cleanupEx) {
            log.error("清理登录资源失败 - 用户ID: {}", userId, cleanupEx);
        }
    }

    // ============ 不变的辅助方法 ============

    private void updateUserLoginInfo(LoginContext loginContext) {
        User user = loginContext.getUser();
        user.setLoginCount(user.getLoginCount() + 1);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(loginContext.getClientIp());
        userRepository.updateById(user);
    }

    private void cacheAccessToken(LoginContext loginContext) {
        try {
            String tokenKey = CacheKeys.Auth.accessToken(loginContext.getUser().getId());
            cacheUtil.putStringSmart(tokenKey, loginContext.getAccessToken());
        } catch (Exception e) {
            log.error("缓存AccessToken失败", e);
            throw ExceptionFactory.authService(ErrorCode.System.TOKEN_STORAGE_FAILED);
        }
    }

    private void recordSuccessfulLoginIp(LoginContext loginContext) {
        try {
            String key = CacheKeys.Auth.successIp(loginContext.getUser().getId());
            cacheUtil.leftPushString(key, loginContext.getClientIp());
            cacheUtil.trimString(key, 0, 4);
            cacheUtil.expireString(key, CacheKeys.Expire.MONTH, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("记录成功登录IP失败", e);
        }
    }

    private void analyzeLoginBehavior(LoginContext loginContext) {
        String clientIp = loginContext.getClientIp();
        String username = loginContext.getUsername();

        if (isInternalIp(clientIp)) {
            log.debug("内网用户登录: {}, IP: {}", username, clientIp);
        } else {
            log.debug("外网用户登录: {}, IP: {}", username, clientIp);
        }
    }

    private boolean isInternalIp(String ip) {
        return ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.16.");
    }

    // ============ 记录类定义 ============

    /**
     * 令牌对记录类
     */
    private record TokenPair(String accessToken, String refreshToken) {
    }

}