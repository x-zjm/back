package com.nianji.auth.model.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会话限制信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionLimitInfo {
    
    /**
     * 当前会话数量
     */
    private Integer currentSessions;
    
    /**
     * 最大允许会话数量
     */
    private Integer maxSessions;
    
    /**
     * 是否达到限制
     */
    private Boolean limitReached;
    
    /**
     * 认证模式
     */
    private String authMode;
    
    /**
     * 提示信息
     */
    private String message;
}