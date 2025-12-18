package com.nianji.auth.service;

import com.nianji.auth.context.LoginContext;
import com.nianji.auth.context.RefreshTokenContext;
import com.nianji.auth.context.RegisterContext;
import com.nianji.auth.context.ResetPasswordContext;
import com.nianji.auth.vo.LoginVO;
import com.nianji.common.reqres.BizResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

/**
 * TODO
 *
 * @author zhangjinming
 * @version 0.0.1
 */
public interface AuthService {

    /**
     * 登录接口
     *
     * @param loginContext
     *         登录相关上下文
     * @return 返回登录结果
     */
    @Transactional
    BizResult<LoginVO> login(LoginContext loginContext);

    /**
     * 注册接口
     *
     * @param registerContext
     *         注册请求上下文
     */
    @Transactional
    BizResult<Void> register(RegisterContext registerContext);

    /**
     * 刷新token
     *
     * @param refreshTokenContext
     *         刷新令牌上下文
     * @return 返回令牌相关信息
     */
    BizResult<LoginVO> refreshToken(RefreshTokenContext refreshTokenContext);

    /**
     * 登出接口
     *
     * @param token
     *         登录token
     */
    BizResult<Void> logout(String token);

    /**
     * 重置密码
     * @param resetPasswordContext 重置密码上下文
     */
    BizResult<Void> resetPassword(ResetPasswordContext resetPasswordContext);

    BizResult<Void> sendResetPasswordEmail(String email);
}
