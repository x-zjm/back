package com.nianji.auth.model.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfo implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 登录时间
     */
    private LocalDateTime loginTime;
    
    /**
     * 最后活动时间
     */
    private LocalDateTime lastActivityTime;
    
    /**
     * 登出时间
     */
    private LocalDateTime logoutTime;
    
    /**
     * 客户端IP
     */
    private String clientIp;
    
    /**
     * 用户代理
     */
    private String userAgent;
    
    /**
     * 访问令牌
     */
    private String accessToken;
    
    /**
     * 刷新令牌
     */
    private String refreshToken;
    
    /**
     * 会话状态
     */
    private SessionStatus status;
    
    /**
     * 登出原因
     */
    private String logoutReason;
    
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
    
    public enum SessionStatus {
        ACTIVE,     // 活跃
        EXPIRED,    // 过期
        REVOKED,    // 已撤销
        LOGGED_OUT  // 已登出
    }
}