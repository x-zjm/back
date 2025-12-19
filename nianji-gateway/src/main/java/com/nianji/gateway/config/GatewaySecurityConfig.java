package com.nianji.gateway.config;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.nianji.gateway.filter.JwtAuthenticationFilter;
import com.nianji.gateway.manager.JwtAuthenticationManager;
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
import org.springframework.util.AntPathMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class GatewaySecurityConfig {

    private final SecurityProperties securityProperties;
    private final CorsProperties corsProperties;
    private final JwtAuthenticationManager jwtAuthenticationManager;
    
    // 使用预编译的路径匹配器提高性能
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    // 缓存公共路径列表以提高访问速度
    private volatile List<String> cachedPublicPaths = new CopyOnWriteArrayList<>();

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        log.info("初始化网关安全配置，公开路径: {}", securityProperties.getPublicPaths());
        
        // 初始化缓存的公共路径
        updateCachedPublicPaths();

        // 创建JWT认证过滤器
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtAuthenticationManager);

        return http
                .csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository())
                        .csrfTokenRequestHandler(csrfTokenRequestHandler())
                        .requireCsrfProtectionMatcher(this::csrfProtectionMatcher))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(securityProperties.getPublicPaths().toArray(new String[0])).permitAll()
                        .anyExchange().authenticated()
                )
                .build();
    }
    
    /**
     * 更新缓存的公共路径列表
     */
    private void updateCachedPublicPaths() {
        this.cachedPublicPaths = new CopyOnWriteArrayList<>(securityProperties.getPublicPaths());
    }

    /**
     * CSRF保护匹配器
     */
    private Mono<ServerWebExchangeMatcher.MatchResult> csrfProtectionMatcher(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        // 对公开路径禁用CSRF保护（使用缓存列表提高性能）
        for (String publicPath : cachedPublicPaths) {
            if (pathMatcher.match(publicPath, path)) {
                return ServerWebExchangeMatcher.MatchResult.notMatch();
            }
        }

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
}