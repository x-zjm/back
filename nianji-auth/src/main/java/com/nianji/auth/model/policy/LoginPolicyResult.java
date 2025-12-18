package com.nianji.auth.model.policy;

import com.nianji.auth.model.session.SessionLimitInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录策略验证结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginPolicyResult {
    
    /**
     * 是否允许登录
     */
    private Boolean allowed;
    
    /**
     * 策略信息
     */
    private LoginPolicy policy;
    
    /**
     * 会话限制信息
     */
    private SessionLimitInfo sessionLimitInfo;
    
    /**
     * 验证消息
     */
    private String message;
    
    /**
     * 是否需要额外验证
     */
    private Boolean requireAdditionalVerification;
    
    /**
     * 验证类型（如：短信、邮箱、二次密码等）
     */
    private String verificationType;
}