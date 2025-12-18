package com.nianji.auth.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Data
@Configuration
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "security.login")
public class AuthSecurityConfig {

    private int maxLoginAttempts = 5;
    private int lockDurationMinutes = 30;
    private int ipMaxAttempts = 20;
    private int ipLockDurationMinutes = 60;
    private boolean enableIpRiskCheck = true;
    private boolean enableConcurrentLoginCheck = true;

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}