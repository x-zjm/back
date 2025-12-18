package com.nianji.auth.context;

import cn.hutool.core.util.ObjectUtil;
import com.nianji.auth.dto.request.RefreshTokenRequest;
import com.nianji.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 刷新令牌相关上下文
 *
 * @author zhangjinming
 * @version 0.0.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenContext {

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 新的token
     */
    private String newAccessToken;

    /**
     * 新的刷新令牌
     */
    private String newRefreshToken;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 用户代理
     */
    private String userAgent;

    public static RefreshTokenContext buildRefreshTokenContext(RefreshTokenRequest refreshTokenRequest) {
        return RefreshTokenContext.builder()
                .refreshToken(refreshTokenRequest.getRefreshToken())
                .build();
    }

    public static RefreshTokenContext buildRefreshTokenContext(LoginContext loginContext) {
        User user = loginContext.getUser();
        return RefreshTokenContext.builder()
                .userId(ObjectUtil.isEmpty(user) ? 0L : user.getId())
                .refreshToken(loginContext.getRefreshToken())
                .clientIp(loginContext.getClientIp())
                .userAgent(loginContext.getUserAgent())
                .build();
    }
}
