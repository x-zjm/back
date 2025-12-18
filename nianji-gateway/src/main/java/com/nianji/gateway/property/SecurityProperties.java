package com.nianji.gateway.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private CookieConfig cookie;

    /**
     * 用户相关操作路径
     */
    private List<String> userPaths;

    /**
     * 系统管理操作路径
     */
    private List<String> adminPaths = new ArrayList<>(List.of(
            "/api/admin/**",
            "/api/system/**"
    ));

    /**
     * 公共路径
     */
    private List<String> publicPaths = new ArrayList<>(List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh-token"
    ));

    @Data
    public static class CookieConfig {
        private int cookieExpire = 1440;
        private boolean secure = true;
        private String sameSite = "Strict";
        private String path = "/";
        private String cookiePath = "/";
        private String cookieName = "XSRF-TOKEN";
        private String headerName = "X-XSRF-TOKEN";
    }

}