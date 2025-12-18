package com.nianji.auth.config;

import com.nianji.common.security.enums.EncryptionAlgorithm;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.lang.annotation.*;

/**
 * 业务模块加密配置注解
 * 各业务模块使用此注解来配置自己的加密方式
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
@ConfigurationProperties(prefix = "security.encryption.auth")
public @interface AuthEncryptionConfig {
    
    /**
     * 业务模块名称
     */
    String value();
    
    /**
     * 支持的加密算法
     */
    EncryptionAlgorithm[] algorithms() default {
        EncryptionAlgorithm.RSA_ECB_OAEP,
        EncryptionAlgorithm.AES_GCM
    };
    
    /**
     * 默认加密算法
     */
    EncryptionAlgorithm defaultAlgorithm() default EncryptionAlgorithm.RSA_ECB_OAEP;
    
    /**
     * 是否启用加密
     */
    boolean enabled() default true;
}