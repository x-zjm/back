package com.nianji.auth.dao.repository;

import com.nianji.auth.entity.LoginLog;

/**
 * TODO
 *
 * @author zhangjinming
 * @version 0.0.1
 */
public interface LoginLogRepository {

    /**
     * 插入登录日志
     *
     * @param loginLog
     *         登录日志
     * @return 插入后生成的日志ID
     */
    int insert(LoginLog loginLog);

}
