package com.nianji.auth.context;

import cn.hutool.core.util.ObjectUtil;
import com.nianji.auth.entity.LoginLog;
import com.nianji.auth.entity.User;
import com.nianji.common.enums.LoginStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 登录日志上下文
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginLogContext {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 登录IP
     */
    private String loginIp;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 登录状态：0-失败，1-成功 see LoginStatusEnum
     */
    private Integer loginStatus;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 构建请求日志上下文
     */
    public static LoginLogContext buildLoginLogContext(LoginContext loginContext) {
        User user = loginContext.getUser();
        return LoginLogContext.builder()
                .userId(ObjectUtil.isEmpty(user) ? null : user.getId())
                .username(loginContext.getUsername())
                .loginIp(loginContext.getClientIp())
                .loginLocation(loginContext.getLocation())
                .userAgent(loginContext.getUserAgent())
                .loginTime(LocalDateTime.now())
                .loginStatus(loginContext.getLoginStatus())
                .failReason(loginContext.getFailReason())
                .build();
    }

    public LoginLog toEntity() {
        return LoginLog.builder()
                .userId(getUserId())
                .username(getUsername())
                .loginIp(getLoginIp())
                .loginLocation(getLoginLocation())
                .userAgent(getUserAgent())
                .loginTime(getLoginTime())
                .loginStatus(getLoginStatus())
                .failReason(getFailReason())
                .build();
    }
}