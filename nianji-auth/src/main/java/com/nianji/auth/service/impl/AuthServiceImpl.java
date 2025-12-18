package com.nianji.auth.service.impl;

import com.nianji.auth.context.LoginContext;
import com.nianji.auth.context.RefreshTokenContext;
import com.nianji.auth.context.RegisterContext;
import com.nianji.auth.context.ResetPasswordContext;
import com.nianji.auth.service.*;
import com.nianji.auth.vo.LoginVO;
import com.nianji.common.reqres.BizResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务 - 主协调服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final LoginService loginService;
    private final TokenService tokenServiceImpl;
    private final UserRegistrationService userRegistrationService;
    private final PasswordResetService passwordResetService;

    @Override
    @Transactional
    public BizResult<LoginVO> login(LoginContext loginContext) {
        return loginService.processLogin(loginContext);
    }

    @Override
    @Transactional
    public BizResult<Void> register(RegisterContext registerContext) {
        return userRegistrationService.registerUser(registerContext);
    }

    @Override
    public BizResult<Void> logout(String token) {
        return tokenServiceImpl.processLogout(token);
    }

    @Override
    public BizResult<LoginVO> refreshToken(RefreshTokenContext refreshTokenContext) {
        return tokenServiceImpl.refreshAccessToken(refreshTokenContext);
    }

    @Override
    public BizResult<Void> sendResetPasswordEmail(String email) {
        return passwordResetService.sendResetEmail(email);
    }

    @Override
    public BizResult<Void> resetPassword(ResetPasswordContext resetPasswordContext) {
        return passwordResetService.processPasswordReset(resetPasswordContext);
    }
}