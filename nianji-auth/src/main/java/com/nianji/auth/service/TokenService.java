package com.nianji.auth.service;

import com.nianji.auth.context.RefreshTokenContext;
import com.nianji.auth.vo.LoginVO;
import com.nianji.common.reqres.BizResult;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 令牌管理服务接口 负责令牌的生成、验证、刷新和注销等生命周期管理
 */
public interface TokenService {

    /**
     * 处理用户注销请求
     *
     * @param token
     *         用户访问令牌
     */
    BizResult<Void> processLogout(String token);

    /**
     * 刷新访问令牌
     *
     * @param refreshTokenContext
     *         刷新令牌
     * @return 返回新的令牌信息
     */
    BizResult<LoginVO> refreshAccessToken(RefreshTokenContext refreshTokenContext);

    /**
     * 将token加入黑名单
     *
     * @param token
     *         待加入黑名单的token
     * @param reason
     *         加入黑名单的原因
     */
    void addTokenToBlacklist(String token, String reason);
}