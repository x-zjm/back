package com.nianji.auth.model.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会话统计信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionStats {
    
    /**
     * 总会话数量
     */
    private Integer totalSessions;
    
    /**
     * 活跃会话数量
     */
    private Integer activeSessions;
    
    /**
     * 今日登录次数
     */
    private Integer todayLogins;
    
    /**
     * 平均会话时长（分钟）
     */
    private Double averageSessionDuration;
}