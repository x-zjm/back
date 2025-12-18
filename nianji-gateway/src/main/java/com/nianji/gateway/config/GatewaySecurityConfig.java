package com.nianji.gateway.config;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.nianji.gateway.filter.JwtAuthenticationFilter;
import com.nianji.gateway.property.CorsProperties;
import com.nianji.gateway.property.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class GatewaySecurityConfig {

    private final SecurityProperties securityProperties;
    private final CorsProperties corsProperties;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        log.info("初始化网关安全配置，公开路径: {}", securityProperties.getPublicPaths());

        return http
                .csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository())
                        .csrfTokenRequestHandler(csrfTokenRequestHandler())
                        .requireCsrfProtectionMatcher(this::csrfProtectionMatcher))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .authenticationManager(authentication -> {
                    if (authentication != null && authentication.isAuthenticated()) {
                        return Mono.just(authentication);
                    }
                    return Mono.empty();
                })
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(securityProperties.getPublicPaths().toArray(new String[0])).permitAll()
                        .anyExchange().authenticated()
                )
                // .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                // .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                // .logout(ServerHttpSecurity.LogoutSpec::disable)
                // // 设置未认证时的处理
                // .exceptionHandling(exceptionHandling -> exceptionHandling
                //         .authenticationEntryPoint((exchange, ex) -> {
                //             log.warn("🚫 未认证访问被拦截 - 路径: {}, 错误: {}",
                //                     exchange.getRequest().getPath().value(), ex.getMessage());
                //             return unauthorized(exchange, "访问被拒绝: 需要认证");
                //         })
                //         .accessDeniedHandler((exchange, denied) -> {
                //             log.warn("🚫 权限不足访问被拦截 - 路径: {}",
                //                     exchange.getRequest().getPath().value());
                //             return forbidden(exchange, "权限不足");
                //         })
                // )

                .build();
    }

    /**
     * CSRF保护匹配器
     */
    private Mono<ServerWebExchangeMatcher.MatchResult> csrfProtectionMatcher(ServerWebExchange exchange) {
        // String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        // 对状态变更的请求启用CSRF保护（POST, PUT, PATCH, DELETE）
        if (method.matches("POST|PUT|PATCH|DELETE")) {
            return ServerWebExchangeMatcher.MatchResult.match();
        }
        return ServerWebExchangeMatcher.MatchResult.notMatch();
    }

    /**
     * CSRF令牌请求处理器
     */
    @Bean
    public ServerCsrfTokenRequestAttributeHandler csrfTokenRequestHandler() {
        ServerCsrfTokenRequestAttributeHandler handler = new ServerCsrfTokenRequestAttributeHandler();
        handler.setTokenFromMultipartDataEnabled(true);
        return handler;
    }

    /**
     * CSRF令牌存储策略 - 使用Cookie存储
     */
    @Bean
    public ServerCsrfTokenRepository csrfTokenRepository() {
        CookieServerCsrfTokenRepository repository = CookieServerCsrfTokenRepository.withHttpOnlyFalse();
        SecurityProperties.CookieConfig cookieConfig = securityProperties.getCookie();

        repository.setCookieCustomizer(cookie -> {
            cookie.maxAge(
                    Duration.ofMinutes(
                            cookieConfig.getCookieExpire()));
            cookie.secure(cookieConfig.isSecure());
            cookie.sameSite(cookieConfig.getSameSite());
            cookie.path(cookieConfig.getPath());
        });

        repository.setCookiePath(cookieConfig.getCookiePath());
        repository.setCookieName(cookieConfig.getCookieName());
        repository.setHeaderName(cookieConfig.getHeaderName());
        return repository;
    }

    /**
     * CORS配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.debug("corsProperties 配置：{}", JSONUtil.toJsonStr(corsProperties));

        CorsConfiguration configuration = new CorsConfiguration();

        // 处理允许的源
        List<String> allowedOrigins = corsProperties.getAllowedOrigins();
        if (CollectionUtil.isEmpty(allowedOrigins) || ObjectUtil.equal("*", allowedOrigins.get(0))) {
            configuration.setAllowedOriginPatterns(List.of("*"));
        } else {
            configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        }

        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setExposedHeaders(corsProperties.getExposedHeaders());
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
    //     exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    //     exchange.getResponse().getHeaders().add("Content-Type", "application/json");
    //
    //     String responseBody = String.format(
    //             "{\"error\":\"Unauthorized\",\"message\":\"%s\",\"path\":\"%s\",\"timestamp\":%d}",
    //             message, exchange.getRequest().getPath().value(), System.currentTimeMillis()
    //     );
    //
    //     DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(responseBody.getBytes());
    //     return exchange.getResponse().writeWith(Mono.just(buffer));
    // }
    //
    // private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
    //     exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
    //     exchange.getResponse().getHeaders().add("Content-Type", "application/json");
    //
    //     String responseBody = String.format(
    //             "{\"error\":\"Forbidden\",\"message\":\"%s\",\"path\":\"%s\",\"timestamp\":%d}",
    //             message, exchange.getRequest().getPath().value(), System.currentTimeMillis()
    //     );
    //
    //     DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(responseBody.getBytes());
    //     return exchange.getResponse().writeWith(Mono.just(buffer));
    // }
}