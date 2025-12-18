package com.nianji.auth.service;

import com.nianji.auth.context.LoginLogContext;

/**
 * 认证日志服务
 *
 * @author zhangjinming
 * @version 0.0.1
 */
public interface AuthLogService {

    /**
     * @param context
     *         登录日记上下文 记录登录请求日志
     */
    void logLoginRequest(LoginLogContext context);
}
