package com.nianji.auth.model.token;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 增强的RefreshToken元数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 登录IP地址
     */
    private String loginIp;

    /**
     * 用户代理信息
     */
    private String userAgent;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedAt;

    /**
     * 是否已撤销
     */
    private boolean revoked = false;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 浏览器类型
     */
    private String browserType;

    /**
     * 操作系统
     */
    private String operatingSystem;

    /**
     * 会话ID
     */
    private String sessionId;

    public RefreshTokenMetadata(Long userId, String loginIp, String userAgent) {
        this.userId = userId;
        this.loginIp = loginIp;
        this.userAgent = userAgent;
        this.createdAt = LocalDateTime.now();
        this.lastUsedAt = LocalDateTime.now();
        // 解析设备信息
        parseUserAgent(userAgent);
        this.sessionId = generateSessionId();
    }

    private void parseUserAgent(String userAgent) {
        // 简化的设备信息解析
        if (userAgent.toLowerCase().contains("mobile")) {
            this.deviceType = "Mobile";
        } else if (userAgent.toLowerCase().contains("tablet")) {
            this.deviceType = "Tablet";
        } else {
            this.deviceType = "Desktop";
        }

        if (userAgent.toLowerCase().contains("chrome")) {
            this.browserType = "Chrome";
        } else if (userAgent.toLowerCase().contains("firefox")) {
            this.browserType = "Firefox";
        } else if (userAgent.toLowerCase().contains("safari")) {
            this.browserType = "Safari";
        } else {
            this.browserType = "Unknown";
        }

        if (userAgent.toLowerCase().contains("windows")) {
            this.operatingSystem = "Windows";
        } else if (userAgent.toLowerCase().contains("mac")) {
            this.operatingSystem = "Mac OS";
        } else if (userAgent.toLowerCase().contains("linux")) {
            this.operatingSystem = "Linux";
        } else if (userAgent.toLowerCase().contains("android")) {
            this.operatingSystem = "Android";
        } else if (userAgent.toLowerCase().contains("ios")) {
            this.operatingSystem = "iOS";
        } else {
            this.operatingSystem = "Unknown";
        }
    }

    private String generateSessionId() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
}