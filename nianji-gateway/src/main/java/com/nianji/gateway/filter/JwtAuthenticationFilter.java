package com.nianji.gateway.filter;

import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.ExceptionFactory;
import com.nianji.common.exception.client.AuthenticationException;
import com.nianji.gateway.manager.JwtAuthenticationManager;
import com.nianji.gateway.model.JwtAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtAuthenticationFilter extends AuthenticationWebFilter {

    // 防止重复执行的标记
    private static final String JWT_FILTER_APPLIED = "JWT_FILTER_APPLIED";

    public JwtAuthenticationFilter(JwtAuthenticationManager authenticationManager) {
        super(authenticationManager);
        setServerAuthenticationConverter(new JwtServerAuthenticationConverter());

        // 设置认证失败处理器 - 直接抛出异常，让全局异常处理器处理
        setAuthenticationFailureHandler((exchange, exception) -> {
            String path = exchange.getExchange().getRequest().getPath().value();

            // 将Spring Security异常转换为自定义异常
            AuthenticationException authException = ExceptionFactory.authentication(
                    ErrorCode.Client.TOKEN_INVALID,
                    "认证失败: " + exception.getMessage()
            );

            log.warn("❌ JWT认证失败 - 路径: {}, 错误码: {}, 消息: {}",
                    path, authException.getCode(), authException.getMessage());

            // 直接抛出异常，让全局异常处理器处理
            return Mono.error(authException);
        });

        log.info("✅ JwtAuthenticationFilter 初始化完成");
    }

    @Override
    public Mono<Void> filter(org.springframework.web.server.ServerWebExchange exchange,
                             org.springframework.web.server.WebFilterChain chain) {

        // 检查是否已经应用过此过滤器
        if (exchange.getAttribute(JWT_FILTER_APPLIED) != null) {
            log.debug("🔄 JWT 过滤器已应用，跳过重复执行 - 请求ID: {}", exchange.getRequest().getId());
            return chain.filter(exchange);
        }

        // 标记此过滤器已应用
        exchange.getAttributes().put(JWT_FILTER_APPLIED, Boolean.TRUE);

        String path = exchange.getRequest().getPath().value();
        String requestId = exchange.getRequest().getId();
        String method = exchange.getRequest().getMethod().name();

        log.debug("🔐 JWT Security Filter 开始 - 请求ID: {}, 方法: {}, 路径: {}", requestId, method, path);

        return super.filter(exchange, chain)
                .doOnSuccess(v -> log.debug("✅ JWT Security Filter 完成 - 请求ID: {}", requestId))
                .doOnError(e -> {
                    if (e instanceof AuthenticationException ex) {
                        log.warn("❌ JWT Security Filter 认证失败 - 请求ID: {}, 错误码: {}",
                                requestId, ex.getCode());
                    } else {
                        log.error("❌ JWT Security Filter 错误 - 请求ID: {}, 错误: {}",
                                requestId, e.getMessage(), e);
                    }
                });
    }

    private static class JwtServerAuthenticationConverter implements ServerAuthenticationConverter {
        @Override
        public Mono<Authentication> convert(ServerWebExchange exchange) {
            return Mono.fromCallable(() -> {
                String token = extractToken(exchange);
                String path = exchange.getRequest().getPath().value();

                if (StringUtils.hasText(token)) {
                    log.debug("🔑 提取到JWT Token - 路径: {}, Token长度: {}", path, token.length());
                    return new JwtAuthenticationToken(null, token, null);
                } else {
                    log.debug("🚫 未找到JWT Token - 路径: {}", path);
                    return null;
                }
            });
        }

        private String extractToken(ServerWebExchange exchange) {
            HttpHeaders headers = exchange.getRequest().getHeaders();
            String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }

            // 也支持从查询参数中获取token（用于WebSocket等场景）
            String tokenParam = exchange.getRequest().getQueryParams().getFirst("token");
            if (StringUtils.hasText(tokenParam)) {
                return tokenParam;
            }

            return null;
        }
    }
}