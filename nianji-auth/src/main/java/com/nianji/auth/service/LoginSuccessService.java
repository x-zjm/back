package com.nianji.auth.service;

import com.nianji.auth.context.LoginContext;
import com.nianji.auth.vo.LoginVO;
import com.nianji.common.reqres.BizResult;

/**
 * 登录成功处理服务接口 负责登录成功后的业务处理，包括令牌生成、用户信息更新、日志记录等
 */
public interface LoginSuccessService {

    /**
     * 处理登录成功后的业务逻辑
     *
     * @param loginContext
     *         登录上下文
     * @return 登录响应信息
     */
    BizResult<LoginVO> handleSuccessfulLogin(LoginContext loginContext);
}