package com.nianji.auth.service.impl;

import com.nianji.auth.config.AuthConfig;
import com.nianji.auth.context.LoginContext;
import com.nianji.auth.model.policy.LoginPolicy;
import com.nianji.auth.model.policy.LoginPolicyResult;
import com.nianji.auth.model.session.SessionInfo;
import com.nianji.auth.model.session.SessionLimitInfo;
import com.nianji.auth.service.LoginPolicyService;
import com.nianji.auth.service.SessionManagementService;
import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.reqres.BizResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * 登录策略服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginPolicyServiceImpl implements LoginPolicyService {

    private final AuthConfig authConfig;
    private final SessionManagementService sessionManagementService;

    @Override
    public BizResult<LoginPolicyResult> validateLoginPolicy(LoginContext loginContext) {
        try {
            Long userId = loginContext.getUser().getId();

            // 不再调用 checkSessionLimit，只做策略验证
            LoginPolicy policy = getUserLoginPolicy(userId);

            LoginPolicyResult result = LoginPolicyResult.builder()
                    .allowed(true)
                    .policy(policy)
                    .message("登录策略验证通过")
                    .build();

            return BizResult.success(result);
        } catch (Exception e) {
            log.error("验证登录策略失败", e);
            return BizResult.fail(ErrorCode.Business.BUSINESS_ERROR, "登录策略验证失败");
        }
    }

    @Override
    public BizResult<Void> applyLoginPolicy(LoginContext loginContext) {
        try {
            Long userId = loginContext.getUser().getId();
            String username = loginContext.getUser().getUsername();

            log.info("应用登录策略 - 用户: {}, 模式: {}", username, authConfig.getMode());

            // 单点登录模式：撤销其他所有会话
            if (authConfig.isSingleSessionMode()) {
                sessionManagementService.revokeAllUserSessions(userId, "SINGLE_SESSION_POLICY");
                log.info("单点登录策略应用 - 用户: {}", username);
            }

            // 会话限制模式：不在这里处理，由调用方处理

            log.debug("应用登录策略完成 - 用户: {}", username);
            return BizResult.success();
        } catch (Exception e) {
            log.error("应用登录策略失败", e);
            return BizResult.fail(ErrorCode.Business.BUSINESS_ERROR, "应用登录策略失败");
        }
    }

    @Override
    public LoginPolicy getUserLoginPolicy(Long userId) {
        // 这里可以根据用户角色、等级等返回不同的策略
        // 目前返回全局配置的策略

        return LoginPolicy.builder()
                .authMode(authConfig.getMode())
                .maxSessions(authConfig.getMaxSessions())
                .sessionTimeout(authConfig.getSessionTimeout())
                .enableSessionEviction(authConfig.getEnableSessionEviction())
                .allowRemoteLogin(true)
                .enableDeviceVerification(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public BizResult<SessionLimitInfo> checkSessionLimit(LoginContext loginContext) {
        // 添加调用栈信息
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String caller = stackTrace.length > 2 ? stackTrace[2].getMethodName() : "unknown";
        log.info("checkSessionLimit 被调用 - 用户: {}, 调用者: {}",
                loginContext.getUser().getUsername(), caller);

        try {
            Long userId = loginContext.getUser().getId();

            // 获取当前活跃会话数（包括刚创建的新会话）
            int currentSessions = sessionManagementService.getActiveSessionCount(userId);
            int maxSessions = getMaxSessionsForUser(userId);

            // 构建准确的消息
            String message;
            if (authConfig.isSingleSessionMode()) {
                message = "单点登录模式";
                maxSessions = 1; // 单点登录强制最大会话数为1
            } else if (authConfig.isMultiSessionMode()) {
                message = String.format("多点登录模式，当前会话数: %d", currentSessions);
            } else {
                message = String.format("会话限制模式，当前: %d/%d", currentSessions, maxSessions);
            }

            SessionLimitInfo limitInfo = SessionLimitInfo.builder()
                    .currentSessions(currentSessions)
                    .maxSessions(maxSessions)
                    .limitReached(currentSessions > maxSessions)
                    .authMode(authConfig.getMode().name()) // 使用配置的实际模式
                    .message(message)
                    .build();

            log.debug("会话限制检查 - 用户: {}, 模式: {}, 会话: {}/{}, 限制达到: {}",
                    loginContext.getUsername(), authConfig.getMode(),
                    currentSessions, maxSessions, limitInfo.getLimitReached());

            return BizResult.success(limitInfo);
        } catch (Exception e) {
            log.error("检查会话限制失败", e);
            return BizResult.fail(ErrorCode.Business.BUSINESS_ERROR, "会话限制检查失败");
        }
    }

    @Override
    public BizResult<Void> handleSessionExceeded(LoginContext loginContext) {
        try {
            Long userId = loginContext.getUser().getId();
            String username = loginContext.getUsername();

            log.info("开始处理会话超限 - 用户: {}, 用户ID: {}", username, userId);

            if (authConfig.isLimitedSessionsMode()) {
                // 重新获取当前会话状态（确保数据最新）
                int currentSessions = sessionManagementService.getActiveSessionCount(userId);
                int maxSessions = getMaxSessionsForUser(userId);

                log.info("会话超限处理 - 用户: {}, 当前会话数: {}, 最大允许: {}",
                        username, currentSessions, maxSessions);

                // 计算需要踢出的会话数量
                int sessionsToRevoke = currentSessions - maxSessions + 1; // +1 为新会话腾出位置
                if (sessionsToRevoke > 0) {
                    // 获取活跃会话并按登录时间排序
                    List<SessionInfo> activeSessions = sessionManagementService.getActiveSessions(userId);

                    log.info("获取到活跃会话数: {} - 用户: {}", activeSessions.size(), username);

                    // 按登录时间升序排序（最早的在前）
                    activeSessions.sort(Comparator.comparing(SessionInfo::getLoginTime));

                    // 踢出最早的 sessionsToRevoke 个会话
                    int revokedCount = 0;
                    for (int i = 0; i < sessionsToRevoke && i < activeSessions.size(); i++) {
                        SessionInfo sessionToRevoke = activeSessions.get(i);
                        log.info("准备踢出会话 - 序号: {}, 会话ID: {}, 登录时间: {}, 用户: {}",
                                i, sessionToRevoke.getSessionId(),
                                sessionToRevoke.getLoginTime(), username);

                        sessionManagementService.revokeSession(
                                sessionToRevoke.getSessionId(),
                                "SESSION_LIMIT_EXCEEDED"
                        );
                        revokedCount++;

                        // 短暂延迟，确保Redis操作完成
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    // 验证踢出结果
                    int afterRevokeSessions = sessionManagementService.getActiveSessionCount(userId);
                    log.info("会话踢出完成 - 用户: {}, 原会话数: {}, 踢出数: {}, 现会话数: {}",
                            username, currentSessions, revokedCount, afterRevokeSessions);

                    if (afterRevokeSessions >= currentSessions) {
                        log.error("会话踢出失败 - 用户: {}, 踢出后会话数未减少", username);
                        return BizResult.fail(ErrorCode.Business.BUSINESS_ERROR, "会话限制处理失败");
                    }
                } else {
                    log.info("无需踢出会话 - 用户: {}, 当前会话数在限制内", username);
                }
            } else {
                log.info("非会话限制模式，跳过踢出逻辑 - 用户: {}, 模式: {}",
                        username, authConfig.getMode());
            }

            return BizResult.success();
        } catch (Exception e) {
            log.error("处理会话超限失败 - 用户: {}", loginContext.getUsername(), e);
            return BizResult.fail(ErrorCode.Business.BUSINESS_ERROR, "处理会话超限失败");
        }
    }

    // ============ 私有方法 ============

    private int getMaxSessionsForUser(Long userId) {
        // 这里可以根据用户角色或等级返回不同的最大会话数
        // 目前返回全局配置
        return authConfig.getMaxSessions();
    }

    private String buildLimitMessage(int current, int max) {
        if (authConfig.isSingleSessionMode()) {
            return "单点登录模式，新登录将踢出其他会话";
        } else if (authConfig.isMultiSessionMode()) {
            return String.format("多点登录模式，当前会话数: %d", current);
        } else {
            return String.format("会话限制模式，当前: %d/%d", current, max);
        }
    }
}