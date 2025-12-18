package com.nianji.common.config;

import com.nianji.common.interceptor.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置 兼容：SpringBoot 3.x
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    public WebConfig() {
        log.debug("WebConfig 被初始化了");
    }

    @Autowired
    private RequestInterceptor requestInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestInterceptor)
                .addPathPatterns("/**")  // 拦截所有路径
                .excludePathPatterns("/error", "/actuator/**");  // 排除错误页面和监控端点
    }

}