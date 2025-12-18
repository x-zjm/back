package com.nianji.auth.model.policy;

import com.nianji.auth.config.AuthConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 登录策略
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginPolicy {
    
    /**
     * 认证模式
     */
    private AuthConfig.AuthMode authMode;
    
    /**
     * 最大会话数量
     */
    private Integer maxSessions;
    
    /**
     * 会话超时时间（秒）
     */
    private Integer sessionTimeout;
    
    /**
     * 是否启用会话踢出
     */
    private Boolean enableSessionEviction;
    
    /**
     * 是否允许异地登录
     */
    private Boolean allowRemoteLogin;
    
    /**
     * 是否启用设备验证
     */
    private Boolean enableDeviceVerification;
    
    /**
     * 策略创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 策略更新时间
     */
    private LocalDateTime updatedAt;
}