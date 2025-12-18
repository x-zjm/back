package com.nianji.gateway.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "gateway-rate-limit")
public class GatewayRateLimitProperties {
    private boolean enabled = true;
    private String redisKeyPrefix = "nianji:gateway:rate_limit";
    private DefaultConfig defaults;
    private List<RateLimitRule> rules = new ArrayList<>();

    @Data
    public static class DefaultConfig {
        private long limit = 100;
        private Duration window = Duration.ofMinutes(1);
    }

    @Data
    public static class RateLimitRule {
        private String path;
        private long limit;
        private Duration window;
        private String description;
        private LimitType type = LimitType.IP; // IP, USER, API_KEY等
        private boolean enabled = true;
    }

    public enum LimitType {
        IP, USER, API_KEY
    }
}