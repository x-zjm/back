package com.nianji.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 认证配置 - 支持单点/多点登录控制
 */
@Data
@Component
@ConfigurationProperties(prefix = "auth")
public class AuthConfig {
    
    /**
     * 认证模式
     */
    private AuthMode mode = AuthMode.MULTI_SESSION;
    
    /**
     * 最大会话数量（多点登录时有效）
     */
    private Integer maxSessions = 5;
    
    /**
     * 会话超时时间（秒）
     */
    private Integer sessionTimeout = 7200;
    
    /**
     * 是否启用新登录踢出旧会话
     */
    private Boolean enableSessionEviction = true;
    
    /**
     * 单点登录配置
     */
    private SSOConfig sso = new SSOConfig();
    
    public enum AuthMode {
        SINGLE_SESSION,    // 单点登录
        MULTI_SESSION,     // 多点登录
        LIMITED_SESSIONS   // 限制会话数量
    }
    
    @Data
    public static class SSOConfig {
        /**
         * 是否启用SSO
         */
        private Boolean enabled = false;
        
        /**
         * 全局会话超时时间（秒）
         */
        private Integer globalSessionTimeout = 7200;
        
        /**
         * 授权码超时时间（秒）
         */
        private Integer authorizationCodeTimeout = 300;
    }
    
    public boolean isSingleSessionMode() {
        return AuthMode.SINGLE_SESSION.equals(mode);
    }
    
    public boolean isMultiSessionMode() {
        return AuthMode.MULTI_SESSION.equals(mode);
    }
    
    public boolean isLimitedSessionsMode() {
        return AuthMode.LIMITED_SESSIONS.equals(mode);
    }
}