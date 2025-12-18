package com.nianji.auth.service.impl;

import com.nianji.auth.context.LoginContext;
import com.nianji.auth.model.session.SessionInfo;
import com.nianji.auth.model.session.SessionStats;
import com.nianji.auth.service.DeviceManagementService;
import com.nianji.auth.service.RefreshTokenCacheService;
import com.nianji.auth.service.SessionManagementService;
import com.nianji.common.config.CacheConfig;
import com.nianji.common.constant.CacheKeys;
import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.ExceptionFactory;
import com.nianji.common.jwt.api.JwtValidator;
import com.nianji.common.reqres.BizResult;
import com.nianji.common.utils.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 会话管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionManagementServiceImpl implements SessionManagementService {

    private final RefreshTokenCacheService refreshTokenCacheService;
    private final DeviceManagementService deviceManagementService;
    private final CacheUtil cacheUtil;
    private final JwtValidator jwtValidator;
    private final CacheConfig cacheConfig;

    @Override
    public void handleLogout(String token, String logoutReason) {
        try {
            Long userId = jwtValidator.extractUserId(token);
            if (userId != null) {
                // 找到当前token对应的会话并撤销
                List<SessionInfo> activeSessions = getActiveSessions(userId);
                for (SessionInfo session : activeSessions) {
                    if (token.equals(session.getAccessToken())) {
                        revokeSession(session.getSessionId(), logoutReason);
                        log.info("处理用户登出 - 用户ID: {}, 会话ID: {}, 原因: {}",
                                userId, session.getSessionId(), logoutReason);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("处理用户登出失败 - Token: {}", maskToken(token), e);
        }
    }

    @Override
    public BizResult<SessionInfo> createSession(LoginContext loginContext) {
        try {
            Long userId = loginContext.getUser().getId();
            String username = loginContext.getUsername();

            // 1. 创建会话前强制检查当前活跃会话数
            int currentSessions = getActiveSessionCount(userId);
            log.info("创建会话前检查 - 用户: {}, 当前活跃会话数: {}", username, currentSessions);

            // 2. 创建会话信息
            SessionInfo sessionInfo = SessionInfo.builder()
                    .sessionId(generateSessionId())
                    .userId(userId)
                    .username(username)
                    .loginTime(LocalDateTime.now())
                    .lastActivityTime(LocalDateTime.now())
                    .clientIp(loginContext.getClientIp())
                    .userAgent(loginContext.getUserAgent())
                    .accessToken(loginContext.getAccessToken())
                    .refreshToken(loginContext.getRefreshToken())
                    .status(SessionInfo.SessionStatus.ACTIVE)
                    .deviceType(parseDeviceType(loginContext.getUserAgent()))
                    .browserType(parseBrowserType(loginContext.getUserAgent()))
                    .operatingSystem(parseOperatingSystem(loginContext.getUserAgent()))
                    .build();

            // 3. 缓存会话信息（同步操作）
            boolean cacheSuccess = cacheSessionSync(sessionInfo);
            if (!cacheSuccess) {
                log.error("会话缓存失败 - 用户: {}, 会话ID: {}", username, sessionInfo.getSessionId());
                return BizResult.fail(ErrorCode.Business.BUSINESS_ERROR, "会话创建失败");
            }

            // 4. 验证会话创建结果
            int afterSessions = getActiveSessionCount(userId);
            log.info("会话创建完成 - 用户: {}, 会话ID: {}, 会话数变化: {} -> {}",
                    username, sessionInfo.getSessionId(), currentSessions, afterSessions);

            if (afterSessions <= currentSessions) {
                log.error("会话创建后计数异常 - 用户: {}, 之前: {}, 之后: {}",
                        username, currentSessions, afterSessions);
                return BizResult.fail(ErrorCode.Business.BUSINESS_ERROR, "会话创建异常");
            }

            return BizResult.success(sessionInfo);
        } catch (Exception e) {
            log.error("创建会话失败", e);
            return BizResult.fail(ErrorCode.Business.BUSINESS_ERROR, "会话创建失败");
        }
    }

    /**
     * 同步缓存会话信息 - 确保Redis操作完成
     */
    private boolean cacheSessionSync(SessionInfo sessionInfo) {
        int retryCount = 0;
        int maxRetries = 3;

        while (retryCount < maxRetries) {
            try {
                Long userId = sessionInfo.getUserId();
                String sessionId = sessionInfo.getSessionId();

                // 1. 存储会话详情
                String sessionKey = CacheKeys.Session.sessionDetail(sessionId);
                cacheUtil.putSmart(sessionKey, sessionInfo);

                // 2. 添加到活跃会话列表
                String sessionsKey = CacheKeys.Session.activeSessionList(userId);
                cacheUtil.leftPush(sessionsKey, sessionInfo);
                cacheUtil.expire(sessionsKey, cacheConfig.getExpire(sessionsKey), TimeUnit.SECONDS);

                // 3. 立即验证缓存结果
                Thread.sleep(50); // 确保Redis操作完成

                // 验证会话详情
                SessionInfo cachedSession = cacheUtil.get(sessionKey);
                if (cachedSession == null) {
                    log.warn("会话详情缓存验证失败，重试 {}/{} - 会话ID: {}",
                            retryCount + 1, maxRetries, sessionId);
                    retryCount++;
                    continue;
                }

                // 验证活跃会话列表
                List<Object> sessions = cacheUtil.range(sessionsKey, 0, -1);
                if (sessions == null) {
                    log.warn("活跃会话列表缓存验证失败，重试 {}/{} - 用户ID: {}",
                            retryCount + 1, maxRetries, userId);
                    retryCount++;
                    continue;
                }

                // 检查新会话是否在列表中
                boolean sessionInList = sessions.stream()
                        .anyMatch(obj -> obj instanceof SessionInfo &&
                                ((SessionInfo) obj).getSessionId().equals(sessionId));

                if (!sessionInList) {
                    log.warn("新会话不在活跃列表中，重试 {}/{} - 会话ID: {}",
                            retryCount + 1, maxRetries, sessionId);
                    retryCount++;
                    continue;
                }

                log.debug("会话缓存成功 - 用户ID: {}, 会话ID: {}, 重试次数: {}",
                        userId, sessionId, retryCount);
                return true;

            } catch (Exception e) {
                log.warn("会话缓存重试 {}/{} 失败: {}", retryCount + 1, maxRetries, e.getMessage());
                retryCount++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.error("会话缓存失败，达到最大重试次数 - 用户ID: {}, 会话ID: {}",
                sessionInfo.getUserId(), sessionInfo.getSessionId());
        return false;
    }

    @Override
    public List<SessionInfo> getActiveSessions(Long userId) {
        try {
            String sessionsKey = CacheKeys.Session.activeSessionList(userId);
            List<Object> sessionObjects = cacheUtil.range(sessionsKey, 0, -1);

            if (sessionObjects == null) {
                return List.of();
            }

            List<SessionInfo> sessions = new ArrayList<>();
            for (Object obj : sessionObjects) {
                if (obj instanceof SessionInfo session) {
                    // 双重验证：检查会话详情是否也存在
                    if (isSessionReallyActive(session.getSessionId())) {
                        sessions.add(session);
                    } else {
                        log.warn("发现无效会话，从活跃列表中移除 - 会话ID: {}", session.getSessionId());
                        // 从活跃列表中移除无效会话
                        removeFromActiveSessions(userId, session.getSessionId());
                    }
                }
            }

            log.debug("获取有效活跃会话 - 用户ID: {}, 数量: {}", userId, sessions.size());
            return sessions;
        } catch (Exception e) {
            log.error("获取活跃会话失败 - 用户ID: {}", userId, e);
            return List.of();
        }
    }

    /**
     * 双重验证会话是否真正活跃
     */
    private boolean isSessionReallyActive(String sessionId) {
        try {
            String sessionKey = CacheKeys.Session.sessionDetail(sessionId);
            SessionInfo session = (SessionInfo) cacheUtil.get(sessionKey);
            return session != null && session.getStatus() == SessionInfo.SessionStatus.ACTIVE;
        } catch (Exception e) {
            log.warn("验证会话活跃状态失败 - 会话ID: {}", sessionId, e);
            return false;
        }
    }


    @Override
    public SessionInfo getSession(String sessionId) {
        try {
            String sessionKey = CacheKeys.Session.sessionDetail(sessionId);
            Object sessionObj = cacheUtil.get(sessionKey);
            return sessionObj instanceof SessionInfo ? (SessionInfo) sessionObj : null;
        } catch (Exception e) {
            log.error("获取会话信息失败 - 会话ID: {}", sessionId, e);
            return null;
        }
    }

    @Override
    public void updateSessionActivity(String sessionId) {
        try {
            SessionInfo session = getSession(sessionId);
            if (session != null && session.getStatus() == SessionInfo.SessionStatus.ACTIVE) {
                session.setLastActivityTime(LocalDateTime.now());
                cacheSession(session);
            }
        } catch (Exception e) {
            log.error("更新会话活动时间失败 - 会话ID: {}", sessionId, e);
        }
    }

    @Override
    public void revokeSession(String sessionId, String reason) {
        try {
            SessionInfo session = getSession(sessionId);
            if (session != null) {
                // 更新会话状态
                session.setStatus(SessionInfo.SessionStatus.REVOKED);
                session.setLogoutTime(LocalDateTime.now());
                session.setLogoutReason(reason);

                // 更新缓存中的会话信息
                cacheSession(session);

                // 从活跃会话列表中移除
                removeFromActiveSessions(session.getUserId(), sessionId);

                // 撤销对应的RefreshToken
                refreshTokenCacheService.revokeRefreshToken(session.getRefreshToken());

                log.info("撤销会话成功 - 会话ID: {}, 用户: {}, 原因: {}",
                        sessionId, session.getUsername(), reason);
            } else {
                log.warn("要撤销的会话不存在 - 会话ID: {}", sessionId);
            }
        } catch (Exception e) {
            log.error("撤销会话失败 - 会话ID: {}", sessionId, e);
        }
    }

    @Override
    public void revokeAllUserSessions(Long userId, String reason) {
        try {
            // 修改：获取所有会话，不限于ACTIVE状态
            List<SessionInfo> allSessions = getActiveSessions(userId);
            log.info("开始撤销用户所有会话 - 用户ID: {}, 会话数: {}", userId, allSessions.size());

            int revokedCount = 0;
            for (SessionInfo session : allSessions) {
                // 修改：撤销所有会话，无论当前状态
                revokeSession(session.getSessionId(), reason);
                revokedCount++;

                // 短暂延迟确保Redis操作完成
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            log.info("撤销用户所有会话完成 - 用户ID: {}, 撤销数量: {}", userId, revokedCount);
        } catch (Exception e) {
            log.error("撤销用户所有会话失败 - 用户ID: {}", userId, e);
        }
    }

    @Override
    public SessionStats getSessionStats(Long userId) {
        List<SessionInfo> activeSessions = getActiveSessions(userId);
        int activeCount = (int) activeSessions.stream()
                .filter(s -> s.getStatus() == SessionInfo.SessionStatus.ACTIVE)
                .count();

        return SessionStats.builder()
                .totalSessions(activeSessions.size())
                .activeSessions(activeCount)
                .build();
    }

    @Override
    public int getActiveSessionCount(Long userId) {
        try {
            List<SessionInfo> activeSessions = getActiveSessions(userId);
            int count = activeSessions.size();

            log.debug("活跃会话计数 - 用户ID: {}, 数量: {}", userId, count);
            return count;
        } catch (Exception e) {
            log.error("获取活跃会话数量失败 - 用户ID: {}", userId, e);
            return 0;
        }
    }

    // ============ 私有方法 ============

    /**
     * 缓存会话信息
     */
    private void cacheSession(SessionInfo sessionInfo) {
        try {
            Long userId = sessionInfo.getUserId();
            String sessionId = sessionInfo.getSessionId();

            // 1. 存储到活跃会话列表
            String sessionsKey = CacheKeys.Session.activeSessionList(userId);
            cacheUtil.leftPush(sessionsKey, sessionInfo);

            // 2. 单独存储会话详情
            String sessionKey = CacheKeys.Session.sessionDetail(sessionId);
            cacheUtil.putSmart(sessionKey, sessionInfo);

            // 3. 设置列表的过期时间
            cacheUtil.expire(sessionsKey, cacheConfig.getExpire(sessionsKey), TimeUnit.SECONDS);

            log.debug("缓存会话信息成功 - 用户ID: {}, 会话ID: {}, 状态: {}",
                    userId, sessionId, sessionInfo.getStatus());

            // 4. 验证缓存结果
            validateCacheResult(userId, sessionId);
        } catch (Exception e) {
            log.error("缓存会话信息失败 - 用户ID: {}, 会话ID: {}",
                    sessionInfo.getUserId(), sessionInfo.getSessionId(), e);
            throw ExceptionFactory.cache(ErrorCode.System.CACHE_ERROR, "会话缓存失败");
        }
    }

    /**
     * 验证缓存结果
     */
    private void validateCacheResult(Long userId, String sessionId) {
        try {
            Thread.sleep(50); // 短暂延迟确保Redis操作完成

            // 验证会话详情是否缓存成功
            String sessionKey = CacheKeys.Session.sessionDetail(sessionId);
            SessionInfo cachedSession = (SessionInfo) cacheUtil.get(sessionKey);

            if (cachedSession == null) {
                log.warn("会话详情缓存验证失败 - 会话ID: {}", sessionId);
            } else {
                log.debug("会话详情缓存验证成功 - 会话ID: {}", sessionId);
            }

            // 验证活跃会话列表
            String sessionsKey = CacheKeys.Session.activeSessionList(userId);
            List<Object> sessions = cacheUtil.range(sessionsKey, 0, -1);
            if (sessions == null || sessions.isEmpty()) {
                log.warn("活跃会话列表缓存验证失败 - 用户ID: {}", userId);
            } else {
                log.debug("活跃会话列表缓存验证成功 - 用户ID: {}, 会话数: {}", userId, sessions.size());
            }
        } catch (Exception e) {
            log.warn("缓存验证过程中出现异常", e);
        }
    }

    /**
     * 从活跃会话列表中移除指定会话
     */
    private void removeFromActiveSessions(Long userId, String sessionId) {
        try {
            String sessionsKey = CacheKeys.Session.activeSessionList(userId);
            List<Object> sessionObjects = cacheUtil.range(sessionsKey, 0, -1);

            if (sessionObjects != null) {
                // 过滤掉要移除的会话
                List<SessionInfo> updatedSessions = sessionObjects.stream()
                        .filter(Objects::nonNull)
                        .map(obj -> (SessionInfo) obj)
                        .filter(session -> !session.getSessionId().equals(sessionId))
                        .collect(Collectors.toList());

                // 删除原列表并重新添加
                cacheUtil.delete(sessionsKey);
                for (SessionInfo session : updatedSessions) {
                    cacheUtil.leftPush(sessionsKey, session);
                }
                cacheUtil.expire(sessionsKey, cacheConfig.getExpire(sessionsKey), TimeUnit.SECONDS);

                log.debug("从活跃会话列表移除会话 - 用户ID: {}, 会话ID: {}, 剩余会话数: {}",
                        userId, sessionId, updatedSessions.size());
            }
        } catch (Exception e) {
            log.error("从活跃会话列表移除会话失败 - 用户ID: {}, 会话ID: {}", userId, sessionId, e);
        }
    }

    private String generateSessionId() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    private String maskToken(String token) {
        if (token == null || token.length() <= 16) {
            return "***";
        }
        return token.substring(0, 8) + "..." + token.substring(token.length() - 8);
    }

    // 设备信息解析方法保持不变...
    private String parseDeviceType(String userAgent) {
        if (userAgent == null || userAgent.trim().isEmpty()) return "Unknown";
        String lowerUserAgent = userAgent.toLowerCase();
        if (lowerUserAgent.contains("mobile")) return "Mobile";
        if (lowerUserAgent.contains("tablet")) return "Tablet";
        return "Desktop";
    }

    private String parseBrowserType(String userAgent) {
        if (userAgent == null || userAgent.trim().isEmpty()) return "Unknown";
        String lowerUserAgent = userAgent.toLowerCase();
        if (lowerUserAgent.contains("chrome")) return "Chrome";
        if (lowerUserAgent.contains("firefox")) return "Firefox";
        if (lowerUserAgent.contains("safari")) return "Safari";
        return "Unknown";
    }

    private String parseOperatingSystem(String userAgent) {
        if (userAgent == null || userAgent.trim().isEmpty()) return "Unknown";
        String lowerUserAgent = userAgent.toLowerCase();
        if (lowerUserAgent.contains("windows")) return "Windows";
        if (lowerUserAgent.contains("mac")) return "Mac OS";
        if (lowerUserAgent.contains("linux")) return "Linux";
        if (lowerUserAgent.contains("android")) return "Android";
        if (lowerUserAgent.contains("ios")) return "iOS";
        return "Unknown";
    }
}