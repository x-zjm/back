package com.nianji.auth.model.token;

import com.nianji.common.enums.TokenTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 令牌信息包装类 包含访问令牌和刷新令牌的完整信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 令牌类型
     */
    private String tokenType = TokenTypeEnum.BEARER.getType();

    /**
     * 访问令牌过期时间（秒）
     */
    private Long expiresIn;

    /**
     * 刷新令牌过期时间（秒）
     */
    private Long refreshExpiresIn;

    /**
     * 建议提前刷新时间（秒）
     */
    private Long refreshBefore = 300L;

    /**
     * 时钟偏差容错时间（秒）
     */
    private Long clockSkew = 60L;

    /**
     * 令牌缓存策略建议
     */
    private String tokenCacheStrategy = "MEMORY_LOCALSTORAGE";

    /**
     * 是否安全Cookie
     */
    private Boolean secureCookie = true;

    /**
     * 令牌颁发时间
     */
    private LocalDateTime issuedAt;

    /**
     * 访问令牌过期时间点
     */
    private LocalDateTime accessTokenExpiresAt;

    /**
     * 刷新令牌过期时间点
     */
    private LocalDateTime refreshTokenExpiresAt;
}