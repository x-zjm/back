package com.nianji.common.jwt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private Long expiration = 86400L; // 默认24小时
    private Long refreshExpiration = 604800L; // 默认7天
    private String issuer = "nianji-system";
    private Integer shortTokenMinutes = 30; // 短期令牌默认30分钟
}