package com.nianji.auth.controller;

import cn.hutool.json.JSONUtil;
import com.nianji.auth.context.LoginContext;
import com.nianji.auth.context.RefreshTokenContext;
import com.nianji.auth.context.RegisterContext;
import com.nianji.auth.context.ResetPasswordContext;
import com.nianji.auth.dto.request.LoginRequest;
import com.nianji.auth.dto.request.RefreshTokenRequest;
import com.nianji.auth.dto.request.RegisterRequest;
import com.nianji.auth.dto.request.ResetPasswordRequest;
import com.nianji.auth.vo.LoginVO;
import com.nianji.auth.service.AuthService;
import com.nianji.common.jwt.util.TokenUtils;
import com.nianji.common.ratelimit.annotation.RateLimit;
import com.nianji.common.constant.RateLimitConstants;
import com.nianji.common.reqres.BizResult;
import com.nianji.common.reqres.RequestModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


import jakarta.servlet.http.HttpServletRequest;


/**
 * 认证管理
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     *
     * @param requestModel
     *         请求入参
     * @param request
     *         登录请求
     * @return 返回登录信息
     */
    @PostMapping("/login")
    @RateLimit(type = RateLimitConstants.RateLimitType.LOGIN_IP,
            message = "登录尝试过于频繁")
    @RateLimit(type = RateLimitConstants.RateLimitType.LOGIN_USER,
            key = "#loginRequest.username",
            message = "该账号登录尝试过于频繁")
    public BizResult<LoginVO> login(@RequestBody RequestModel<LoginRequest> requestModel,
                                    HttpServletRequest request) {

        log.info("AuthController login request:{}", JSONUtil.toJsonStr(requestModel));

        requestModel.validateFullWithExpire();
        LoginRequest loginRequest = requestModel.getRequestData();
        LoginContext loginContext = LoginContext.buildLoginContext(loginRequest, request);

        log.info("用户登录请求 - 用户名: {}, IP: {}, UserAgent: {}",
                loginRequest.getUsername(),
                loginContext.getClientIp(),
                loginContext.getUserAgent());

        BizResult<LoginVO> loginVO = authService.login(loginContext);

        log.info("用户登录完成 - 用户名: {}, 结果: {}",
                loginRequest.getUsername(), loginVO.isSuccess());
        log.info("AuthController login response:{}", JSONUtil.toJsonStr(loginVO));

        return loginVO;

    }

    /**
     * 用户注册
     *
     * @param requestModel
     *         请求入参
     */
    @PostMapping("/register")
    @RateLimit(type = RateLimitConstants.RateLimitType.REGISTER_IP,
            message = "注册请求过于频繁")
    public BizResult<Void> register(@RequestBody RequestModel<RegisterRequest> requestModel) {
        log.info("AuthController register request:{}", JSONUtil.toJsonStr(requestModel));

        requestModel.validateFullWithExpire();
        RegisterRequest registerRequest = requestModel.getRequestData();

        RegisterContext registerContext = RegisterContext.buildRegisterContext(registerRequest);

        BizResult<Void> register = authService.register(registerContext);

        log.info("AuthController register response: ok");

        return register;
    }

    @PostMapping("/refresh-token")
    @RateLimit(type = RateLimitConstants.RateLimitType.REFRESH_TOKEN,
            key = "#requestData",
            message = "令牌刷新过于频繁")
    public BizResult<LoginVO> refreshToken(@RequestBody RequestModel<RefreshTokenRequest> requestModel) {
        log.info("AuthController refreshToken request:{}", JSONUtil.toJsonStr(requestModel));

        requestModel.validateFullWithExpire();
        RefreshTokenRequest refreshTokenRequest = requestModel.getRequestData();

        RefreshTokenContext refreshTokenContext = RefreshTokenContext.buildRefreshTokenContext(refreshTokenRequest);

        BizResult<LoginVO> loginVO = authService.refreshToken(refreshTokenContext);

        log.info("AuthController refreshToken response:{}", JSONUtil.toJsonStr(loginVO));
        return loginVO;
    }

    @PostMapping("/logout")
    public BizResult<Void> logout(@RequestHeader("Authorization") String token) {
        log.info("AuthController logout start...");

        BizResult<Void> bearer = authService.logout(TokenUtils.extractToken(token));

        log.info("AuthController logout response: ok");
        return bearer;
    }

    @PostMapping("/reset-password")
    public BizResult<Void> resetPassword(@RequestHeader("Authorization") String token,
                                         @RequestBody RequestModel<ResetPasswordRequest> requestModel) {
        log.info("AuthController resetPassword request:{}", JSONUtil.toJsonStr(requestModel));

        requestModel.validateFullWithExpire();
        ResetPasswordRequest resetPasswordRequest = requestModel.getRequestData();

        BizResult<Void> bearer = authService.resetPassword(
                ResetPasswordContext.buildResetPasswordContext(resetPasswordRequest,
                        TokenUtils.extractToken(token)));

        log.info("AuthController resetPassword response: ok");
        return bearer;
    }
}