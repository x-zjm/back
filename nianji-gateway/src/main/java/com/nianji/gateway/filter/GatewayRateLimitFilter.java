package com.nianji.gateway.filter;

import cn.hutool.json.JSONUtil;
import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.reqres.Result;
import com.nianji.gateway.config.DynamicRateLimitConfig;
import com.nianji.gateway.property.GatewayRateLimitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayRateLimitFilter implements GlobalFilter, Ordered {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final DynamicRateLimitConfig dynamicRateLimitConfig;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 检查限流是否启用
        if (!dynamicRateLimitConfig.isEnabled()) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getPath().value();
        String clientIp = getClientIp(exchange);

        // 查找匹配的限流规则
        GatewayRateLimitProperties.RateLimitRule rule = findMatchingRule(path);

        if (rule == null) {
            // 使用默认规则
            rule = getDefaultRule();
        }

        String rateLimitKey = buildRateLimitKey(rule.getPath(), clientIp, rule.getType());
        long limit = rule.getLimit();
        Duration window = rule.getWindow();

        return checkRateLimit(rateLimitKey, limit, window)
                .flatMap(allowed -> {
                    if (allowed) {
                        return chain.filter(exchange);
                    } else {
                        log.warn("请求被限流 - IP: {}, 路径: {}, 规则: {}/{}",
                                clientIp, path, limit, window);
                        return rateLimitedResponse(exchange);
                    }
                });
    }

    private GatewayRateLimitProperties.RateLimitRule findMatchingRule(String requestPath) {
        return dynamicRateLimitConfig.getRules().stream()
                .filter(GatewayRateLimitProperties.RateLimitRule::isEnabled)
                .filter(rule -> pathMatcher.match(rule.getPath(), requestPath))
                .findFirst()
                .orElse(null);
    }

    private GatewayRateLimitProperties.RateLimitRule getDefaultRule() {
        GatewayRateLimitProperties.DefaultConfig defaults = dynamicRateLimitConfig.getDefaults();
        GatewayRateLimitProperties.RateLimitRule defaultRule = new GatewayRateLimitProperties.RateLimitRule();
        defaultRule.setPath("/**");
        defaultRule.setLimit(defaults.getLimit());
        defaultRule.setWindow(defaults.getWindow());
        defaultRule.setType(GatewayRateLimitProperties.LimitType.IP);
        defaultRule.setDescription("默认限流规则");
        return defaultRule;
    }

    private Mono<Boolean> checkRateLimit(String key, long limit, Duration window) {
        return redisTemplate.opsForValue().increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        // 第一次设置过期时间
                        return redisTemplate.expire(key, window)
                                .then(Mono.just(count <= limit));
                    }
                    return Mono.just(count <= limit);
                })
                .defaultIfEmpty(true); // Redis异常时放行
    }

    private String getClientIp(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = exchange.getRequest().getRemoteAddress() != null ?
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
        }
        return ip.split(",")[0].trim();
    }

    private String buildRateLimitKey(String path, String identifier, GatewayRateLimitProperties.LimitType type) {
        String normalizedPath = path.replace("/", "_").replace("*", "ALL");
        return String.format("%s:%s:%s:%s",
                dynamicRateLimitConfig.getRedisKeyPrefix(),
                normalizedPath,
                type.name().toLowerCase(),
                identifier);
    }

    private Mono<Void> rateLimitedResponse(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");


        String body = JSONUtil.toJsonStr(Result.fail(ErrorCode.Client.RATE_LIMIT_EXCEEDED));
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                .bufferFactory().wrap(bytes)));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}