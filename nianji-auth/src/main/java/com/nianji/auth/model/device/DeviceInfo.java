package com.nianji.auth.model.device;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 设备信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfo {
    
    /**
     * 设备指纹
     */
    private String deviceFingerprint;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * IP地址
     */
    private String ip;
    
    /**
     * 用户代理信息
     */
    private String userAgent;
    
    /**
     * 登录时间
     */
    private LocalDateTime loginTime;
    
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
     * 信任级别
     */
    private DeviceTrustLevel trustLevel;
    
    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedTime;
    
    /**
     * 使用次数
     */
    private Integer usageCount;

    public DeviceInfo(String loginIp, String userAgent, LocalDateTime now) {
        this.ip = loginIp;
        this.userAgent = userAgent;
        this.loginTime = now;
    }
}

