package com.nianji.common.context;

import cn.hutool.core.util.StrUtil;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MDC上下文初始化器 - 用于全链路追踪和日志增强
 */
@Component
public class MdcContextInitializer {

    // MDC键常量
    public static final String TRACE_ID = "traceId";
    public static final String SPAN_ID = "spanId";
    public static final String REQUEST_ID = "requestId";
    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";
    public static final String CLIENT_IP = "clientIp";
    public static final String URI = "uri";
    public static final String METHOD = "method";

    /**
     * 初始化MDC上下文
     */
    public void initializeMdcContext() {
        // 设置请求ID作为追踪ID
        String requestId = CustomRequestContext.getRequestId();
        if (StrUtil.isNotBlank(requestId)) {
            MDC.put(REQUEST_ID, requestId);
            MDC.put(TRACE_ID, requestId);
        }

        // 设置请求相关信息
        String uri = CustomRequestContext.getRequestUri();
        if (StrUtil.isNotBlank(uri)) {
            MDC.put(URI, uri);
        }

        String method = CustomRequestContext.getRequestMethod();
        if (StrUtil.isNotBlank(method)) {
            MDC.put(METHOD, method);
        }
    }

    /**
     * 设置用户相关信息
     */
    public void setUserContext(String userId, String username) {
        if (StrUtil.isNotBlank(userId)) {
            MDC.put(USER_ID, userId);
        }
        if (StrUtil.isNotBlank(username)) {
            MDC.put(USERNAME, username);
        }
    }

    /**
     * 设置客户端IP
     */
    public void setClientIp(String clientIp) {
        if (StrUtil.isNotBlank(clientIp)) {
            MDC.put(CLIENT_IP, clientIp);
        }
    }

    /**
     * 清除MDC上下文
     */
    public void clearMdcContext() {
        MDC.remove(TRACE_ID);
        MDC.remove(SPAN_ID);
        MDC.remove(REQUEST_ID);
        MDC.remove(USER_ID);
        MDC.remove(USERNAME);
        MDC.remove(CLIENT_IP);
        MDC.remove(URI);
        MDC.remove(METHOD);
    }

    /**
     * 获取当前MDC上下文快照
     */
    public Map<String, String> getMdcContextSnapshot() {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return contextMap != null ? new ConcurrentHashMap<>(contextMap) : new ConcurrentHashMap<>();
    }
}