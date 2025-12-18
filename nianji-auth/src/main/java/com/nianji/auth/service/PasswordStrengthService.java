package com.nianji.auth.service;

import com.nianji.common.reqres.BizResult;

/**
 * TODO
 *
 * @author zhangjinming
 * @version 0.0.1
 */
public interface PasswordStrengthService {

    /**
     * 验证密码强度
     *
     * @param password
     *         密码
     */
    BizResult<Void> validatePasswordStrength(String password);
}
