package com.nianji.auth.context;

import com.nianji.auth.dto.request.LoginRequest;
import com.nianji.auth.entity.User;
import com.nianji.auth.model.device.DeviceAnalysisResult;
import com.nianji.auth.model.session.SessionLimitInfo;
import com.nianji.auth.service.impl.LoginSuccessServiceImpl;
import com.nianji.common.utils.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录相关上下文
 *
 * @author zhangjinming
 * @version 0.0.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginContext {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 登录地址
     */
    private String location;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * HTTP请求对象，用于获取客户端信息
     */
    private HttpServletRequest request;

    /**
     * 用户信息
     */
    private User user;

    /**
     * 连接token
     */
    private String accessToken;

    /**
     * 刷新token
     */
    private String refreshToken;

    /**
     * 登录状态：0-失败，1-成功 see LoginStatusEnum
     */
    private Integer loginStatus;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * token过期时间(s)
     */
    private Long expiresIn;

    /**
     * refreshToken过期时间(s)
     */
    private Long refreshExpiresIn;

    /**
     * 会话限制信息
     */
    private SessionLimitInfo sessionLimitInfo;

    /**
     * 设备分析结果记录类
     */
    private DeviceAnalysisResult deviceAnalysisResult;

    public static LoginContext buildLoginContext(LoginRequest loginRequest, HttpServletRequest request) {
        String ipAddr = IpUtil.getIpAddr(request);
        return LoginContext.builder()
                .username(loginRequest.getUsername())
                .password(loginRequest.getPassword())
                .clientIp(ipAddr)
                .location(IpUtil.getLocation(ipAddr))
                .request(request)
                .userAgent(request.getHeader("User-Agent"))
                .build();
    }
}
