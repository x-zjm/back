package com.nianji.gateway.config;

import com.nianji.gateway.property.GatewayRateLimitProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DynamicRateLimitConfig {
    
    @Getter
    private volatile List<GatewayRateLimitProperties.RateLimitRule> rules;
    private final GatewayRateLimitProperties gatewayRateLimitProperties;

    public DynamicRateLimitConfig(GatewayRateLimitProperties gatewayRateLimitProperties) {
        this.gatewayRateLimitProperties = gatewayRateLimitProperties;
        this.rules = List.copyOf(gatewayRateLimitProperties.getRules());
    }

    @EventListener
    public void onRefreshEvent(ContextRefreshedEvent event) {
        // 配置刷新时重新加载规则
        this.rules = List.copyOf(gatewayRateLimitProperties.getRules());
        log.info("限流规则已刷新，当前规则数量: {}", rules.size());
    }

    public boolean isEnabled() {
        return gatewayRateLimitProperties.isEnabled();
    }
    
    public String getRedisKeyPrefix() {
        return gatewayRateLimitProperties.getRedisKeyPrefix();
    }
    
    public GatewayRateLimitProperties.DefaultConfig getDefaults() {
        return gatewayRateLimitProperties.getDefaults();
    }
}