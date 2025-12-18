package com.nianji.auth.service.impl;

import com.nianji.auth.context.RegisterContext;
import com.nianji.auth.dao.repository.UserRepository;
import com.nianji.auth.entity.User;
import com.nianji.auth.filter.UserBloomFilterService;
import com.nianji.auth.service.*;
import com.nianji.common.constant.UserConstants;
import com.nianji.common.enums.UserStatusEnum;
import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.reqres.BizResult;
import com.nianji.common.utils.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 增强的用户注册服务 - 支持密码加密传输
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegistrationServiceImpl implements UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordStrengthService passwordStrengthService;
    private final PasswordTransmissionService passwordTransmissionService;
    private final UserBloomFilterService userBloomFilterService;
    private final UserCacheService userCacheService;
    private final UserUniquenessService userUniquenessService;

    @Override
    @Transactional
    public BizResult<Void> registerUser(RegisterContext registerContext) {
        String username = registerContext.getUsername();
        String encryptedPassword = registerContext.getPassword();

        // 1. 验证加密数据格式
        if (!passwordTransmissionService.validateEncryptedData(encryptedPassword)) {
            return BizResult.fail(ErrorCode.Client.PARAM_ERROR, "注册失败，请稍后重试");
        }

        // 2. 解密前端传输的密码
        String plainPassword = passwordTransmissionService.decryptPassword(encryptedPassword);

        // // 3. 验证密码强度
        // BizResult<Void> strengthCheck = passwordStrengthService.validatePasswordStrength(plainPassword);
        // if (!strengthCheck.checkSuccess()) {
        //     return strengthCheck;
        // }

        // 4. 唯一性检查
        BizResult<Void> uniquenessResult = userUniquenessService.checkUserUniqueness(registerContext);
        if (!uniquenessResult.isSuccess()) {
            return uniquenessResult;
        }

        // 5. 创建用户
        User user = buildUserFromRequest(registerContext, plainPassword);
        if (userRepository.insert(user) <= 0) {
            return BizResult.fail(ErrorCode.System.DATABASE_ERROR, "注册失败，请稍后重试");
        }

        // 6. 异步处理
        asyncPostRegistrationProcessing(user);

        log.info("用户注册成功: {}, ID: {}, 加密方式: {}",
                username, user.getId(), passwordTransmissionService.getCurrentEncryptionInfo());
        return BizResult.success();

    }

    private User buildUserFromRequest(RegisterContext registerContext, String password) {
        String nickname = registerContext.getNickname();
        String username = registerContext.getUsername();

        return User.builder()
                .publicId(CommonUtil.generateUUID())
                .username(username)
                .password(passwordEncoder.encode(password)) // 使用BCrypt加密存储
                .phone(registerContext.getPhone())
                .email(registerContext.getEmail())
                .nickname(nickname != null ? nickname :
                        UserConstants.DEFAULT_NICKNAME_PREFIX + System.currentTimeMillis())
                .status(UserStatusEnum.NORMAL.getCode())
                .loginCount(0)
                .build();
    }

    @Async
    @Override
    public void asyncPostRegistrationProcessing(User user) {
        try {
            // 更新布隆过滤器
            userBloomFilterService.addUserToBloomFilter(user);
            // 预热用户缓存
            userCacheService.refreshUserCache(user);
            log.debug("用户 {} 注册后处理完成", user.getUsername());
        } catch (Exception e) {
            log.error("用户注册后处理失败: {}", user.getUsername(), e);
        }
    }
}