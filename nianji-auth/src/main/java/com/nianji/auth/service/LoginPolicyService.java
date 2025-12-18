package com.nianji.auth.service;

import com.nianji.auth.context.LoginContext;
import com.nianji.auth.model.policy.LoginPolicy;
import com.nianji.auth.model.policy.LoginPolicyResult;
import com.nianji.auth.model.session.SessionLimitInfo;
import com.nianji.common.reqres.BizResult;

/**
 * 登录策略服务 - 专注登录规则控制
 */
public interface LoginPolicyService {
    
    /**
     * 验证登录策略
     *
     * @param loginContext 登录上下文
     * @return 策略验证结果
     */
    BizResult<LoginPolicyResult> validateLoginPolicy(LoginContext loginContext);
    
    /**
     * 应用登录策略
     *
     * @param loginContext 登录上下文
     * @return 应用结果
     */
    BizResult<Void> applyLoginPolicy(LoginContext loginContext);
    
    /**
     * 获取用户登录策略
     *
     * @param userId 用户ID
     * @return 登录策略
     */
    LoginPolicy getUserLoginPolicy(Long userId);
    
    /**
     * 检查会话限制
     *
     * @param loginContext 登录上下文
     * @return 会话限制信息
     */
    BizResult<SessionLimitInfo> checkSessionLimit(LoginContext loginContext);
    
    /**
     * 处理会话超限情况
     *
     * @param loginContext 登录上下文
     * @return 处理结果
     */
    BizResult<Void> handleSessionExceeded(LoginContext loginContext);
}