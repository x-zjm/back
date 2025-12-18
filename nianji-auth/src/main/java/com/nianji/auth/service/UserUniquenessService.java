package com.nianji.auth.service;

import com.nianji.auth.context.RegisterContext;
import com.nianji.common.reqres.BizResult;

/**
 * TODO
 *
 * @author zhangjinming
 * @version 0.0.1
 */
public interface UserUniquenessService {

    /**
     * 检查用户唯一性（带缓存优化）
     *
     * @param registerContext
     *         注册相关上下文
     */
    BizResult<Void> checkUserUniqueness(RegisterContext registerContext);
}
