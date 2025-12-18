package com.nianji.common.jwt.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * JWT详细信息DTO
 */
@Data
@Builder
public class JwtDetails {
    private String subject;
    private Long userId;
    private String issuer;
    private Date issuedAt;
    private Date expiration;
    private String tokenType;
    private String csrfToken;
    private long remainingTime;
    private boolean expired;
    
    public boolean isAboutToExpire(long thresholdMinutes) {
        return remainingTime > 0 && remainingTime <= thresholdMinutes * 60 * 1000;
    }
}