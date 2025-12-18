package com.nianji.auth.service.impl;

import com.nianji.auth.context.LoginLogContext;
import com.nianji.auth.dao.repository.LoginLogRepository;
import com.nianji.auth.service.AuthLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 认证日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthLogServiceImpl implements AuthLogService {

    private final LoginLogRepository loginLogRepository;

    @Override
    public void logLoginRequest(LoginLogContext loginLogContext) {

        try {
            loginLogRepository.insert(loginLogContext.toEntity());
        } catch (Exception e) {
            log.error("记录登录日志失败", e);
        }
    }
}