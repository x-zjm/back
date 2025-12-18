package com.nianji.auth.service;

import com.nianji.auth.context.LoginContext;
import com.nianji.auth.model.session.SessionInfo;
import com.nianji.auth.model.session.SessionStats;
import com.nianji.common.reqres.BizResult;

import java.util.List;

/**
 * 会话管理服务 - 专注会话生命周期管理
 */
public interface SessionManagementService {

    /**
     * 创建新会话
     *
     * @param loginContext
     *         登录上下文
     * @return 会话信息
     */
    BizResult<SessionInfo> createSession(LoginContext loginContext);

    /**
     * 获取用户活跃会话
     *
     * @param userId
     *         用户ID
     * @return 活跃会话列表
     */
    List<SessionInfo> getActiveSessions(Long userId);

    /**
     * 获取会话信息
     *
     * @param sessionId
     *         会话ID
     * @return 会话信息
     */
    SessionInfo getSession(String sessionId);

    /**
     * 更新会话活动时间
     *
     * @param sessionId
     *         会话ID
     */
    void updateSessionActivity(String sessionId);

    /**
     * 撤销会话
     *
     * @param sessionId
     *         会话ID
     * @param reason
     *         撤销原因
     */
    void revokeSession(String sessionId, String reason);

    /**
     * 撤销用户所有会话
     *
     * @param userId
     *         用户ID
     * @param reason
     *         撤销原因
     */
    void revokeAllUserSessions(Long userId, String reason);

    /**
     * 获取会话统计信息
     *
     * @param userId
     *         用户ID
     * @return 会话统计
     */
    SessionStats getSessionStats(Long userId);

    /**
     * 获取用户活跃会话数量
     *
     * @param userId
     *         用户ID
     * @return 活跃会话数量
     */
    int getActiveSessionCount(Long userId);

    /**
     * 处理用户登出
     */
    void handleLogout(String token, String logoutReason);
}