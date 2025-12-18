package com.nianji.gateway.filter;

import com.nianji.common.jwt.dto.JwtUserInfo;
import com.nianji.gateway.property.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserInfoForwardFilter implements GlobalFilter, Ordered {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final SecurityProperties securityProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // 检查是否为公开路径
        if (isPublicPath(path)) {
            log.debug("公共路径：{}", path);
            return chain.filter(exchange);
        }

        // // 从SecurityContext中获取认证信息，并转发到下游服务
        // return ReactiveSecurityContextHolder.getContext()
        //         .flatMap(securityContext -> {
        //             if (securityContext.getAuthentication() != null &&
        //                     securityContext.getAuthentication().isAuthenticated()) {
        //
        //                 // 获取JWT用户信息
        //                 Object principal = securityContext.getAuthentication().getPrincipal();
        //                 if (principal instanceof JwtUserInfo userInfo) {
        //
        //                     // 添加用户信息到请求头，供下游服务使用
        //                     ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
        //                             .header("X-User-Id", userInfo.getUserId().toString())
        //                             .header("X-Username", userInfo.getUsername())
        //                             .header("X-Token-Type", userInfo.getTokenType())
        //                             .build();
        //
        //                     log.debug("转发用户信息到下游服务 - 用户: {}, 路径: {}", userInfo.getUsername(), path);
        //                     // return chain.filter(exchange).contextWrite(context -> context.put("USER_INFO", userInfo));
        //                     return chain.filter(exchange.mutate().request(mutatedRequest).build());
        //                 }
        //             }
        //
        //             // 如果没有认证信息，继续执行
        //             return chain.filter(exchange);
        //         })
        //         .switchIfEmpty(chain.filter(exchange));
        return chain.filter(exchange);
    }

    private boolean isPublicPath(String path) {
        return securityProperties.getPublicPaths()
                .stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    public int getOrder() {
        // 在安全认证之后执行
        return Ordered.LOWEST_PRECEDENCE;
    }
}