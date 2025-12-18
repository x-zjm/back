package com.nianji.common.jwt.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * JWT用户信息DTO
 */
@Data
@Builder
public class JwtUserInfo {
    private String username;
    private Long userId;
    private List<String> roles;
    private List<String> permissions;
    private Date issuedAt;
    private Date expiration;
    private String tokenType;
    private String csrfToken;

    public boolean isValid() {
        return username != null && userId != null && expiration != null;
    }

    public boolean isValidWithCsrf(String expectedCsrfToken) {
        return isValid() && csrfToken != null && csrfToken.equals(expectedCsrfToken);
    }

    public long getRemainingTime() {
        if (expiration == null) return -1;
        return expiration.getTime() - System.currentTimeMillis();
    }

    public boolean isExpired() {
        return getRemainingTime() <= 0;
    }

    public boolean willExpireSoon(long thresholdMs) {
        return getRemainingTime() <= thresholdMs;
    }
}