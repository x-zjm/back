package com.nianji.common.context;

import cn.hutool.core.util.StrUtil;
import com.nianji.common.constant.CommonConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 增强的请求上下文持有器 - 集成耗时监控
 */
@Slf4j
public class CustomRequestContext {
    private static final ThreadLocal<String> REQUEST_ID_CACHE = new ThreadLocal<>();
    private static final ThreadLocal<Long> REQUEST_START_TIME_CACHE = new ThreadLocal<>();
    private static final ThreadLocal<String> REQUEST_URI_CACHE = new ThreadLocal<>();
    private static final ThreadLocal<String> REQUEST_METHOD_CACHE = new ThreadLocal<>();

    // 用于统计接口耗时（可选）
    private static final ConcurrentMap<String, RequestStats> REQUEST_STATS_MAP = new ConcurrentHashMap<>();

    private CustomRequestContext() {
    }

    /**
     * 初始化请求上下文（在拦截器中调用）
     */
    public static void initRequestContext(String requestUri, String requestMethod) {
        String requestId = Optional.ofNullable(getCurrentRequest())
                .map(request -> request.getHeader(CommonConstants.REQUEST_ID_HEADER))
                .orElse(null);

        if (StrUtil.isBlank(requestId)) {
            requestId = generateRequestId();
        }

        REQUEST_ID_CACHE.set(requestId);
        REQUEST_START_TIME_CACHE.set(System.currentTimeMillis());
        REQUEST_URI_CACHE.set(requestUri);
        REQUEST_METHOD_CACHE.set(requestMethod);

        log.debug("初始化请求上下文 - 请求ID: {}, 路径: {}, 方法: {}",
                requestId, requestUri, requestMethod);
    }

    /**
     * 获取当前请求ID
     */
    public static String getRequestId() {
        String requestId = REQUEST_ID_CACHE.get();
        return requestId != null ? requestId : generateRequestId();
    }

    /**
     * 获取请求开始时间
     */
    public static Long getStartTime() {
        return REQUEST_START_TIME_CACHE.get();
    }

    /**
     * 获取请求URI
     */
    public static String getRequestUri() {
        return REQUEST_URI_CACHE.get();
    }

    /**
     * 获取请求方法
     */
    public static String getRequestMethod() {
        return REQUEST_METHOD_CACHE.get();
    }

    /**
     * 计算请求耗时
     */
    public static long calculateRequestDuration() {
        Long startTime = REQUEST_START_TIME_CACHE.get();
        if (startTime == null) {
            return 0L;
        }
        return System.currentTimeMillis() - startTime;
    }

    /**
     * 记录请求统计信息
     */
    public static void recordRequestStats(boolean success, String errorCode) {
        String uri = getRequestUri();
        String method = getRequestMethod();
        long duration = calculateRequestDuration();

        if (uri != null && method != null) {
            String key = method + ":" + uri;
            REQUEST_STATS_MAP.compute(key, (k, stats) -> {
                if (stats == null) {
                    stats = new RequestStats();
                }
                stats.recordRequest(duration, success, errorCode);
                return stats;
            });

            // 记录详细日志
            logRequestCompletion(success, errorCode, duration);
        }
    }

    /**
     * 记录请求完成日志
     */
    private static void logRequestCompletion(boolean success, String errorCode, long duration) {
        String requestId = getRequestId();
        String uri = getRequestUri();
        String method = getRequestMethod();

        if (success) {
            log.info("请求完成 - 请求ID: {}, 方法: {}, 路径: {}, 耗时: {}ms",
                    requestId, method, uri, duration);
        } else {
            log.warn("请求失败 - 请求ID: {}, 方法: {}, 路径: {}, 错误码: {}, 耗时: {}ms",
                    requestId, method, uri, errorCode, duration);
        }

        // 慢请求告警（超过3秒）
        if (duration > 3000) {
            log.warn("慢请求告警 - 请求ID: {}, 方法: {}, 路径: {}, 耗时: {}ms",
                    requestId, method, uri, duration);
        }
    }

    /**
     * 获取请求统计信息（用于监控）
     */
    public static ConcurrentMap<String, RequestStats> getRequestStats() {
        return new ConcurrentHashMap<>(REQUEST_STATS_MAP);
    }

    /**
     * 清除线程上下文（在拦截器中调用）
     */
    public static void clear() {
        REQUEST_ID_CACHE.remove();
        REQUEST_START_TIME_CACHE.remove();
        REQUEST_URI_CACHE.remove();
        REQUEST_METHOD_CACHE.remove();
    }

    private static String generateRequestId() {
        return "G" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    /**
     * 获取当前 HttpServletRequest
     */
    private static jakarta.servlet.http.HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 请求统计信息内部类
     */
    public static class RequestStats {
        private long totalRequests;
        private long successfulRequests;
        private long failedRequests;
        private long totalDuration;
        private long maxDuration;
        private long minDuration = Long.MAX_VALUE;

        public void recordRequest(long duration, boolean success, String errorCode) {
            totalRequests++;
            totalDuration += duration;
            maxDuration = Math.max(maxDuration, duration);
            minDuration = Math.min(minDuration, duration);

            if (success) {
                successfulRequests++;
            } else {
                failedRequests++;
            }
        }

        // Getters
        public long getTotalRequests() {
            return totalRequests;
        }

        public long getSuccessfulRequests() {
            return successfulRequests;
        }

        public long getFailedRequests() {
            return failedRequests;
        }

        public double getAverageDuration() {
            return totalRequests > 0 ? (double) totalDuration / totalRequests : 0;
        }

        public long getMaxDuration() {
            return maxDuration;
        }

        public long getMinDuration() {
            return minDuration == Long.MAX_VALUE ? 0 : minDuration;
        }

        public double getSuccessRate() {
            return totalRequests > 0 ? (double) successfulRequests / totalRequests * 100 : 0;
        }
    }
}