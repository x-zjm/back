package com.nianji.auth.service.impl;

import com.nianji.auth.context.ResetPasswordContext;
import com.nianji.auth.dao.repository.UserRepository;
import com.nianji.auth.entity.User;
import com.nianji.auth.service.*;
import com.nianji.common.constant.CacheKeys;
import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.jwt.api.JwtValidator;
import com.nianji.common.reqres.BizResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 增强的密码重置服务 - 支持密码加密传输
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPasswordServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordTransmissionService passwordTransmissionService;
    private final UserCacheService userCacheService;
    private final UserQueryService userQueryService;
    private final TokenService tokenService;

    private final JwtValidator jwtValidator;

    @Override
    @Transactional
    public BizResult<Void> processPasswordReset(ResetPasswordContext resetPasswordContext) {
        String token = resetPasswordContext.getToken();

        String encryptedNewPassword = resetPasswordContext.getNewPassword();
        String encryptedOldPassword = resetPasswordContext.getOldPassword();

        // 1. 验证加密数据格式
        if (!passwordTransmissionService.validateEncryptedData(encryptedNewPassword) ||
                !passwordTransmissionService.validateEncryptedData(encryptedOldPassword)) {
            return BizResult.fail(ErrorCode.Client.PARAM_ERROR, "密码格式错误");
        }

        // 2. 解密密码
        String newPassword = passwordTransmissionService.decryptPassword(encryptedNewPassword);
        String oldPassword = passwordTransmissionService.decryptPassword(encryptedOldPassword);

        // // 3. 验证新密码强度
        // BizResult<Void> strengthCheck = passwordStrengthService.validatePasswordStrength(newPassword);
        // if (!strengthCheck.checkSuccess()) {
        //     return strengthCheck;
        // }

        Long userId = jwtValidator.extractUserId(token);

        // 4. 查询用户
        User user = userQueryService.getUserById(userId);
        if (user == null) {
            return BizResult.fail(ErrorCode.Client.PARAM_ERROR, "用户不存在");
        }

        // 5. 验证原密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return BizResult.fail(ErrorCode.Client.PARAM_ERROR, "原密码不正确");
        }

        // 6. 更新密码
        String newPasswordHash = passwordEncoder.encode(newPassword);
        user.setPassword(newPasswordHash);

        int updated = userRepository.updateById(user);
        if (updated <= 0) {
            return BizResult.fail(ErrorCode.Business.DATA_NOT_FOUND, "密码重置失败");
        }

        // 7. 清除用户缓存和会话
        userCacheService.evictUserCache(user.getUsername());
        userCacheService.clearAllUserSessions(user.getId());

        // 8. 将access token加入黑名单
        tokenService.addTokenToBlacklist(token, "logout");

        log.info("密码重置成功 - 用户: {}, 加密方式: {}",
                user.getUsername(), passwordTransmissionService.getCurrentEncryptionInfo());
        return BizResult.success(null, "密码重置成功");

    }

    @Override
    public BizResult<Void> sendResetEmail(String email) {
        try {
            // 1. 验证邮箱格式
            if (!isValidEmail(email)) {
                return BizResult.fail(ErrorCode.Client.PARAM_ERROR, "邮箱格式不正确");
            }

            // 2. 检查用户是否存在
            User user = userRepository.selectByEmail(email);
            if (user == null) {
                // 出于安全考虑，即使邮箱不存在也返回成功
                log.info("重置密码请求 - 邮箱不存在: {}", email);
                return BizResult.success(null, "如果邮箱存在，重置链接已发送");
            }

            // 3. 异步发送重置邮件
            sendResetPasswordEmailAsync(user.getEmail(), user.getUsername());

            log.info("重置密码邮件已发送: {}, 用户: {}", email, user.getUsername());
            return BizResult.success(null, "如果邮箱存在，重置链接已发送");
        } catch (Exception e) {
            log.error("发送重置密码邮件失败: {}", email, e);
            return BizResult.fail(ErrorCode.System.SYSTEM_ERROR, "发送重置邮件失败，请稍后重试");
        }
    }

    private boolean isValidEmail(String email) {
        if (email == null) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    @Async
    @Override
    public void sendResetPasswordEmailAsync(String email, String username) {
        try {
            // 这里实现实际的邮件发送逻辑
            // 生成重置令牌等

            log.debug("重置密码邮件已异步发送: {}, 用户: {}", email, username);
        } catch (Exception e) {
            log.error("异步发送重置密码邮件失败: {}", email, e);
        }
    }
}