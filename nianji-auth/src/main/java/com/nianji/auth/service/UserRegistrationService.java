package com.nianji.auth.service;

import com.nianji.auth.context.RegisterContext;
import com.nianji.auth.entity.User;
import com.nianji.common.reqres.BizResult;

/**
 * 用户注册服务接口 负责新用户的注册流程，包括数据验证、唯一性检查、用户创建等
 */
public interface UserRegistrationService {

    /**
     * 注册新用户
     *
     * @param registerContext
     *         注册请求上下文
     * @return 注册结果
     */
    BizResult<Void> registerUser(RegisterContext registerContext);

    /**
     * 异步注册后处理（布隆过滤器更新、缓存预热等）
     *
     * @param user
     *         新创建的用户
     */
    void asyncPostRegistrationProcessing(User user);
}