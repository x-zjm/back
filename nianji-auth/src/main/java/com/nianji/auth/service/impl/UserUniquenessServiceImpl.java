package com.nianji.auth.service.impl;

import cn.hutool.core.util.StrUtil;
import com.nianji.auth.context.RegisterContext;
import com.nianji.auth.dao.repository.UserRepository;
import com.nianji.auth.filter.UserBloomFilterService;
import com.nianji.auth.model.cache.CacheCheckResult;
import com.nianji.auth.service.UserUniquenessService;
import com.nianji.common.constant.CacheKeys;
import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.reqres.BizResult;
import com.nianji.common.utils.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 用户唯一性检查服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserUniquenessServiceImpl implements UserUniquenessService {

    private final UserRepository userRepository;
    private final UserBloomFilterService userBloomFilterService;

    private final CacheUtil cacheUtil;

    @Override
    public BizResult<Void> checkUserUniqueness(RegisterContext registerContext) {

        // 1. 布隆过滤器检查
        if (!needDatabaseCheck(registerContext)) {
            return BizResult.success();
        }

        // 2. 缓存检查
        CacheCheckResult cacheResult = checkWithCache(registerContext);
        if (cacheResult.hasConflict()) {
            return BizResult.fail(ErrorCode.Client.PARAM_ERROR, cacheResult.getConflictMessage());
        }

        registerContext.setCacheResult(cacheResult);

        // 3. 数据库检查（只检查缓存中没有的字段）
        return checkWithDatabase(registerContext);
    }

    private boolean needDatabaseCheck(RegisterContext registerContext) {
        boolean usernameExists = userBloomFilterService.mightUsernameExist(registerContext.getUsername());
        boolean emailExists = userBloomFilterService.mightEmailExist(registerContext.getEmail());
        String phone = registerContext.getPhone();
        boolean phoneExists = phone != null && userBloomFilterService.mightPhoneExist(phone);

        log.debug("布隆过滤器检查 - 用户名: {}, 邮箱: {}, 手机号: {}",
                usernameExists, emailExists, phoneExists);

        if (!usernameExists && !emailExists
                && (StrUtil.isBlank(phone) || !phoneExists)) {
            log.debug("布隆过滤器确认所有标识符都不存在，跳过数据库检查");
            return false;
        }

        log.debug("布隆过滤器提示部分标识符可能存在，需要进行数据库确认");
        return true;
    }

    private CacheCheckResult checkWithCache(RegisterContext registerContext) {
        CacheCheckResult result = new CacheCheckResult();

        // 检查用户名缓存
        String usernameKey = CacheKeys.User.usernameExists(registerContext.getUsername());
        Boolean usernameCached = cacheUtil.get(usernameKey);
        if (Boolean.TRUE.equals(usernameCached)) {
            result.setUsernameExists(true);
        }

        // 检查邮箱缓存
        String emailKey = CacheKeys.User.emailExists(registerContext.getEmail());
        Boolean emailCached = cacheUtil.get(emailKey);
        if (Boolean.TRUE.equals(emailCached)) {
            result.setEmailExists(true);
        }

        // 检查手机号缓存
        String phone = registerContext.getPhone();
        if (StrUtil.isNotBlank(phone)) {
            String phoneKey = CacheKeys.User.phoneExists(phone);
            Boolean phoneCached = cacheUtil.get(phoneKey);
            if (Boolean.TRUE.equals(phoneCached)) {
                result.setPhoneExists(true);
            }
        }

        return result;
    }

    private BizResult<Void> checkWithDatabase(RegisterContext registerContext) {
        String username = registerContext.getUsername();
        String email = registerContext.getEmail();
        String phone = registerContext.getPhone();
        CacheCheckResult cacheResult = registerContext.getCacheResult();

        try {
            // 只检查缓存中没有确认的字段
            if (!cacheResult.isUsernameExists()) {
                if (userRepository.existsByUsername(username)) {
                    // 更新缓存
                    cacheUsernameExists(username);
                    return BizResult.fail(ErrorCode.Client.PARAM_ERROR, "用户名已存在");
                }
            }

            if (!cacheResult.isEmailExists()) {
                if (userRepository.existsByEmail(email)) {
                    // 更新缓存
                    cacheEmailExists(email);
                    return BizResult.fail(ErrorCode.Client.PARAM_ERROR, "邮箱已被注册");
                }
            }

            if (!cacheResult.isPhoneExists() && StrUtil.isNotBlank(phone)) {
                if (userRepository.existsByPhone(phone)) {
                    // 更新缓存
                    cachePhoneExists(phone);
                    return BizResult.fail(ErrorCode.Client.PARAM_ERROR, "手机号已被注册");
                }
            }

            return BizResult.success();

        } catch (Exception e) {
            log.error("数据库唯一性检查失败", e);
            return BizResult.fail(ErrorCode.System.SYSTEM_BUSY);
        }
    }

    private void cacheUsernameExists(String username) {
        String key = CacheKeys.User.usernameExists(username);
        cacheUtil.putSmart(key, true);
    }

    private void cacheEmailExists(String email) {
        String key = CacheKeys.User.emailExists(email);
        cacheUtil.putSmart(key, true);
    }

    private void cachePhoneExists(String phone) {
        String key = CacheKeys.User.phoneExists(phone);
        cacheUtil.putSmart(key, true);
    }

}