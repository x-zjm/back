package com.nianji.auth.vo;

import com.nianji.auth.context.LoginContext;
import com.nianji.auth.entity.User;
import com.nianji.auth.model.device.DeviceAnalysisResult;
import com.nianji.auth.model.session.SessionLimitInfo;
import com.nianji.common.enums.TokenTypeEnum;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    // 认证信息
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    // token过期时间(s)
    private Long expiresIn;
    // refreshToken过期时间(s)
    private Long refreshExpiresIn;

    // 用户基本信息
    private String username;
    private String nickname;
    private String avatar;

    // 系统信息
    private LocalDateTime loginTime;
    private String clientIp;

    // ============ 新增字段 ============

    /**
     * 会话限制信息
     */
    private SessionLimitInfo sessionLimitInfo;

    /**
     * 设备变更风险
     */
    private Boolean deviceChangeRisk;

    /**
     * 认证模式
     */
    private String authMode;

    /**
     * 当前会话数量
     */
    private Integer currentSessions;

    /**
     * 最大会话数量
     */
    private Integer maxSessions;

    /**
     * 是否需要设备验证
     */
    private Boolean requireDeviceVerification;

    /**
     * 设备信任级别
     */
    private String deviceTrustLevel;

    /**
     * 增强的构建方法 - 包含会话和设备信息
     */
    public static LoginVO buildLoginVo(LoginContext loginContext) {
        User user = loginContext.getUser();
        SessionLimitInfo sessionLimitInfo = loginContext.getSessionLimitInfo();
        DeviceAnalysisResult deviceAnalysisResult = loginContext.getDeviceAnalysisResult();
        return LoginVO.builder()
                .accessToken(loginContext.getAccessToken())
                .refreshToken(loginContext.getRefreshToken())
                .tokenType(TokenTypeEnum.BEARER.getType())
                .expiresIn(loginContext.getExpiresIn())
                .refreshExpiresIn(loginContext.getRefreshExpiresIn())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .loginTime(LocalDateTime.now())
                .clientIp(loginContext.getClientIp())
                .sessionLimitInfo(sessionLimitInfo)
                .deviceChangeRisk(deviceAnalysisResult.getRisk())
                .authMode(sessionLimitInfo != null ? sessionLimitInfo.getAuthMode() : null)
                .currentSessions(sessionLimitInfo != null ? sessionLimitInfo.getCurrentSessions() : null)
                .maxSessions(sessionLimitInfo != null ? sessionLimitInfo.getMaxSessions() : null)
                .requireDeviceVerification(Boolean.TRUE.equals(deviceAnalysisResult.getRisk()))
                .deviceTrustLevel(deviceAnalysisResult.getTrustLevel())
                .build();
    }
}