package com.nianji.auth.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.nianji.auth.context.LoginContext;
import com.nianji.auth.context.LoginLogContext;
import com.nianji.auth.entity.User;
import com.nianji.auth.service.*;
import com.nianji.auth.vo.LoginVO;
import com.nianji.common.enums.LoginStatusEnum;
import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.reqres.BizResult;
import com.nianji.common.enums.UserStatusEnum;
import com.nianji.common.security.enums.EncryptionAlgorithm;
import com.nianji.common.utils.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 增强的登录处理服务 - 支持密码加密传输
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final UserQueryService userQueryService;
    private final LoginSecurityService loginSecurityService;
    private final LoginSuccessService loginSuccessService;
    private final AuthLogService authLogService;
    private final PasswordTransmissionService passwordTransmissionService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public BizResult<LoginVO> processLogin(LoginContext loginContext) {
        // String username = loginContext.getUsername();
        String encryptedPassword = loginContext.getPassword();

        // try {
        // 1. 验证加密数据格式
        if (!passwordTransmissionService.validateEncryptedData(encryptedPassword)) {
            loginContext.setFailReason("密码格式错误");
            loginSecurityService.recordFailedAttempt(loginContext);
            return BizResult.fail(ErrorCode.Client.INVALID_CREDENTIALS);
        }

        // 2. 解密前端传输的密码
        String plainPassword = passwordTransmissionService.decryptPassword(encryptedPassword, EncryptionAlgorithm.RSA_ECB_OAEP);
        loginContext.setPassword(plainPassword);

        // 3. 检查是否重复登录
        // User user = cacheUtil.get(CacheConstants.User.buildInfoByUsernameKey(username));
        // if (ObjectUtil.isNotNull(user)
        //         && cacheUtil.hasKey(CacheConstants.Auth.buildAccessTokenKey(user.getId()))) {
        //     loginContext.setUser(user);
        //     loginContext.setAccessToken(cacheUtil.getString(
        //             CacheConstants.Auth.buildAccessTokenKey(user.getId())));
        //     return BizResult.success(LoginVO.buildLoginVo(loginContext));
        // }

        // 4. 安全防护检查
        BizResult<Void> securityCheck = loginSecurityService.checkLoginSecurity(loginContext);
        if (!securityCheck.isSuccess()) {
            return BizResult.fail(securityCheck.getCode(), securityCheck.getMsg());
        }

        // 5. 用户验证
        BizResult<User> userResult = validateUserCredentials(loginContext);
        if (!userResult.isSuccess()) {
            return BizResult.fail(userResult.getCode(), userResult.getMsg());
        }

        // 6. 登录成功处理
        return loginSuccessService.handleSuccessfulLogin(loginContext);

        // } catch (Exception e) {
        //     log.error("用户登录失败: {}, IP: {}, 加密方式: {}",
        //         username, clientIp, passwordTransmissionService.getCurrentEncryptionInfo(), e);
        //     loginContext.setFailReason("系统异常");
        //     loginSecurityService.recordFailedAttempt(loginContext);
        //     loginContext.setLoginStatus(LoginStatusEnum.FAIL.getCode());
        //     authLogService.logLoginRequest(LoginLogContext.buildLoginLogContext(loginContext));
        //     return BizResult.fail(ErrorCode.Server.SERVER_ERROR);
        // }
    }

    private BizResult<User> validateUserCredentials(LoginContext loginContext) {
        String username = loginContext.getUsername();
        String clientIp = loginContext.getClientIp();
        String password = loginContext.getPassword();

        // 1. 用户存在性检查
        User user = userQueryService.getUserByUsername(username);
        if (ObjectUtil.isEmpty(user)) {
            loginContext.setFailReason("用户不存在");
            loginSecurityService.recordFailedAttempt(loginContext);
            loginContext.setLoginStatus(LoginStatusEnum.FAIL.getCode());
            authLogService.logLoginRequest(LoginLogContext.buildLoginLogContext(loginContext));
            return BizResult.fail(ErrorCode.Client.INVALID_CREDENTIALS);
        }

        loginContext.setUser(user);

        // 2. 用户状态检查
        if (!UserStatusEnum.NORMAL.getCode().equals(user.getStatus())) {
            loginContext.setFailReason("用户状态异常");
            loginSecurityService.recordFailedAttempt(loginContext);
            loginContext.setLoginStatus(LoginStatusEnum.FAIL.getCode());
            authLogService.logLoginRequest(LoginLogContext.buildLoginLogContext(loginContext));
            return BizResult.fail(ErrorCode.Client.INVALID_CREDENTIALS);
        }

        // 3. 密码验证
        if (!passwordEncoder.matches(password, user.getPassword())) {
            loginContext.setFailReason("密码错误");
            loginSecurityService.recordFailedAttempt(loginContext);
            loginContext.setLoginStatus(LoginStatusEnum.FAIL.getCode());
            authLogService.logLoginRequest(LoginLogContext.buildLoginLogContext(loginContext));
            loginSecurityService.checkAndLockAccount(username, clientIp);
            return BizResult.fail(ErrorCode.Client.INVALID_CREDENTIALS);
        }

        return BizResult.success(user);
    }
}