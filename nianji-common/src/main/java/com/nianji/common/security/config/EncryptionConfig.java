package com.nianji.common.security.config;

import com.nianji.common.security.enums.EncryptionAlgorithm;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 加解密配置
 * 注意：敏感密钥信息不应明文存储在配置文件中，应使用外部密钥管理系统或配置中心的加密功能
 */
@Data
@Component
@ConfigurationProperties(prefix = "security.encryption")
public class EncryptionConfig {

    /**
     * 是否启用加密
     */
    private boolean enabled = true;

    /**
     * 支持的加密算法
     */
    private Set<EncryptionAlgorithm> algorithms = new HashSet<>();

    /**
     * 默认加密算法
     */
    private EncryptionAlgorithm defaultAlgorithm = EncryptionAlgorithm.RSA_ECB_OAEP;

    /**
     * 默认密钥版本
     */
    private String defaultKeyVersion = "v1";

    /**
     * RSA公钥（Base64编码）- 仅用于开发测试，生产环境应使用外部密钥管理系统
     * @deprecated 不推荐在配置文件中明文存储，将在后续版本中移除
     */
    @Deprecated
    private String rsaPublicKey;

    /**
     * RSA私钥（Base64编码）- 仅用于开发测试，生产环境应使用外部密钥管理系统
     * @deprecated 不推荐在配置文件中明文存储，将在后续版本中移除
     */
    @Deprecated
    private String rsaPrivateKey;

    /**
     * AES密钥（Base64编码）- 仅用于开发测试，生产环境应使用外部密钥管理系统
     * @deprecated 不推荐在配置文件中明文存储，将在后续版本中移除
     */
    @Deprecated
    private String aesKey;

    /**
     * 密钥轮换间隔（小时）
     */
    private int rotationIntervalHours = 24;

    /**
     * 是否启用自动轮换
     */
    private boolean autoRotation = true;
}