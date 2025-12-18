package com.nianji.auth.service;

import com.nianji.auth.context.ResetPasswordContext;
import com.nianji.common.reqres.BizResult;
import org.springframework.scheduling.annotation.Async;

/**
 * 密码重置服务接口 负责密码重置流程，包括重置邮件发送、令牌验证、密码更新等
 */
public interface PasswordResetService {

    /**
     * 发送密码重置邮件
     *
     * @param email
     *         用户邮箱地址
     * @return 发送结果
     */
    BizResult<Void> sendResetEmail(String email);

    /**
     * 处理密码重置请求
     *
     * @param resetPasswordContext
     *         重置密码上下文
     * @return 重置结果
     */
    BizResult<Void> processPasswordReset(ResetPasswordContext resetPasswordContext);

    /**
     * 异步发送重置密码邮件
     *
     * @param email
     *         收件人邮箱
     * @param username
     *         用户名
     */
    @Async
    void sendResetPasswordEmailAsync(String email, String username);
}