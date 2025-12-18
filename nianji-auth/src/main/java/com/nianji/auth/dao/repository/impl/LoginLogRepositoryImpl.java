package com.nianji.auth.dao.repository.impl;

import com.nianji.auth.dao.mapper.LoginLogMapper;
import com.nianji.auth.dao.repository.LoginLogRepository;
import com.nianji.auth.entity.LoginLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * TODO
 *
 * @author zhangjinming
 * @version 0.0.1
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class LoginLogRepositoryImpl implements LoginLogRepository {

    private final LoginLogMapper loginLogMapper;

    @Override
    public int insert(LoginLog loginLog) {
        loginLog.setCreateTime(LocalDateTime.now());
        loginLog.setUpdateTime(LocalDateTime.now());
        return loginLogMapper.insert(loginLog);
    }
}
