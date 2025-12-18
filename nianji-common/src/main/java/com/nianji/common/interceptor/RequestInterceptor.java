package com.nianji.common.interceptor;

import com.nianji.common.constant.CommonConstants;
import com.nianji.common.context.CustomRequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 增强的请求拦截器
 */
@Slf4j
@Component
public class RequestInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 初始化请求上下文，包含URI和方法信息
        CustomRequestContext.initRequestContext(
                request.getRequestURI(),
                request.getMethod()
        );


        // 将请求ID设置到响应头中
        response.setHeader(CommonConstants.REQUEST_ID_HEADER, CustomRequestContext.getRequestId());

        // 记录请求开始日志
        log.debug("请求开始 - 请求ID: {}, 路径: {}, 方法: {}",
                CustomRequestContext.getRequestId(),
                request.getRequestURI(),
                request.getMethod());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            // 判断请求是否成功（根据HTTP状态码）
            boolean success = response.getStatus() < 400;
            String errorCode = success ? null : String.valueOf(response.getStatus());

            // 记录请求统计信息
            CustomRequestContext.recordRequestStats(success, errorCode);

        } finally {
            // 清除线程上下文
            CustomRequestContext.clear();
        }
    }
}