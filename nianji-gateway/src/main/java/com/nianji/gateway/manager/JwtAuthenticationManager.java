package com.nianji.gateway.manager;

import com.nianji.common.constant.CacheKeys;
import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.ExceptionFactory;
import com.nianji.common.exception.client.AuthenticationException;
import com.nianji.common.jwt.dto.JwtUserInfo;
import com.nianji.gateway.model.JwtAuthenticationToken;
import com.nianji.gateway.service.GatewayJwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final GatewayJwtService gatewayJwtService;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        // 如果已经认证过了，直接返回
        if (authentication != null && authentication.isAuthenticated()) {
            return Mono.just(authentication);
        }

        log.debug("🔄 开始JWT认证流程");
        return Mono.justOrEmpty(authentication)
                .filter(auth -> auth.getCredentials() != null)
                .flatMap(auth -> {
                    String token = auth.getCredentials().toString();
                    log.debug("🔐 处理Token认证 - Token长度: {}", token.length());
                    return processTokenAuthentication(token);
                })
                .cast(Authentication.class)
                .doOnNext(auth -> {
                    if (auth.getPrincipal() instanceof JwtUserInfo) {
                        JwtUserInfo userInfo = (JwtUserInfo) auth.getPrincipal();
                        log.debug("✅ JWT认证完成 - 用户: {}, 角色数: {}",
                                userInfo.getUsername(), userInfo.getRoles() != null ? userInfo.getRoles().size() : 0);
                    }
                })
                .doOnError(e -> {
                    if (e instanceof AuthenticationException) {
                        AuthenticationException ex = (AuthenticationException) e;
                        log.warn("❌ JWT认证失败 - 错误码: {}, 消息: {}", ex.getCode(), ex.getMessage());
                    } else {
                        log.error("❌ JWT认证异常 - 异常类型: {}", e.getClass().getSimpleName(), e);
                    }
                });
    }

    private Mono<JwtAuthenticationToken> processTokenAuthentication(String token) {
        // 1. 检查Token是否为空
        if (!StringUtils.hasText(token)) {
            log.warn("Token为空");
            return Mono.error(ExceptionFactory.authentication(
                    ErrorCode.Client.TOKEN_MISSING,
                    "Token不能为空"
            ));
        }

        // 2. 检查Token黑名单
        return checkTokenBlacklist(token)
                .flatMap(blacklisted -> {
                    if (blacklisted) {
                        log.warn("🚫 JWT Token在黑名单中");
                        return Mono.error(ExceptionFactory.authentication(
                                ErrorCode.Client.TOKEN_INVALID,
                                "Token已失效"
                        ));
                    }

                    // 3. 使用GatewayJwtService验证Token
                    try {
                        JwtUserInfo userInfo = gatewayJwtService.validateAndGetUserInfo(token);

                        // 4. 创建认证对象
                        List<SimpleGrantedAuthority> authorities = extractAuthorities(userInfo);
                        JwtAuthenticationToken authenticated = new JwtAuthenticationToken(
                                userInfo, token, authorities);
                        authenticated.setAuthenticated(true);

                        log.debug("✅ JWT认证成功 - 用户: {}", userInfo.getUsername());
                        return Mono.just(authenticated);
                    } catch (AuthenticationException e) {
                        // 直接转换为Mono.error，确保在响应式流中正确传播
                        log.debug("转换为Mono.error的认证异常: {}", e.getMessage());
                        return Mono.error(e);
                    }
                })
                .onErrorResume(e -> {
                    // 捕获其他异常并转换为认证异常
                    if (!(e instanceof AuthenticationException)) {
                        log.error("JWT认证处理异常", e);
                        return Mono.error(ExceptionFactory.authentication(
                                ErrorCode.Client.TOKEN_INVALID,
                                "认证处理异常"
                        ));
                    }
                    return Mono.error(e);
                });
    }

    private Mono<Boolean> checkTokenBlacklist(String token) {
        String key = CacheKeys.Security.blacklistedToken(token);
        return redisTemplate.hasKey(key)
                .onErrorReturn(false)
                .defaultIfEmpty(false)
                .doOnError(e -> log.error("检查Token黑名单失败", e));
    }

    private List<SimpleGrantedAuthority> extractAuthorities(JwtUserInfo userInfo) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if (userInfo.getRoles() != null) {
            authorities.addAll(
                    userInfo.getRoles().stream()
                            .filter(StringUtils::hasText)
                            .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                            .map(SimpleGrantedAuthority::new)
                            .toList()
            );
        }

        if (userInfo.getPermissions() != null) {
            authorities.addAll(
                    userInfo.getPermissions().stream()
                            .filter(StringUtils::hasText)
                            .map(SimpleGrantedAuthority::new)
                            .toList()
            );
        }

        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        log.debug("提取用户权限 - 用户: {}, 角色数: {}, 权限数: {}",
                userInfo.getUsername(),
                userInfo.getRoles() != null ? userInfo.getRoles().size() : 0,
                userInfo.getPermissions() != null ? userInfo.getPermissions().size() : 0);

        return authorities;
    }
}