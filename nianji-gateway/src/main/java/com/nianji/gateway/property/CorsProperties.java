package com.nianji.gateway.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * TODO
 *
 * @author zhangjinming
 * @version 0.0.1
 */
@Data
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    /**
     * 允许的请求地址
     */
    private List<String> allowedOrigins;

    /**
     * 允许的方法
     */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");

    /**
     * 允许的请求头
     */
    private List<String> allowedHeaders;

    /**
     * 对外暴露的响应头
     */
    private List<String> exposedHeaders;

    /**
     * 是否允许跨域请求携带凭证
     */
    private boolean allowCredentials = true;

    /**
     * 预检请求最长时间
     */
    private long maxAge = 3600L;
}
