package com.nianji.auth.service;

import com.nianji.auth.context.LoginContext;
import com.nianji.common.reqres.BizResult;

/**
 * 登录安全服务接口 负责登录过程中的安全防护，包括防暴力破解、IP限制、账户锁定等
 */
public interface LoginSecurityService {

    /**
     * 检查登录安全状态
     *
     * @param loginContext
     *         登录相关上下文
     * @return 安全检查结果
     */
    BizResult<Void> checkLoginSecurity(LoginContext loginContext);

    /**
     * 记录登录失败尝试
     *
     * @param loginContext
     *         登录上下文
     */
    void recordFailedAttempt(LoginContext loginContext);

    /**
     * 检查并锁定账户（如果达到失败次数阈值）
     *
     * @param username
     *         用户名
     * @param loginIp
     *         登录IP地址
     */
    void checkAndLockAccount(String username, String loginIp);

    /**
     * 清除失败计数（登录成功时调用）
     *
     * @param loginContext
     *         登录相关上下文
     */
    void clearFailedAttempts(LoginContext loginContext);

}