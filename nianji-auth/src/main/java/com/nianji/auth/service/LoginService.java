package com.nianji.auth.service;

import com.nianji.auth.context.LoginContext;
import com.nianji.auth.vo.LoginVO;
import com.nianji.common.reqres.BizResult;

/**
 * 登录服务接口 负责处理用户登录的核心业务流程，包括安全验证、用户认证等
 */
public interface LoginService {

    /**
     * 处理用户登录请求
     *
     * @param loginContext
     *         登录相关上下文
     * @return 登录结果，包含令牌和用户信息
     */
    BizResult<LoginVO> processLogin(LoginContext loginContext);
}