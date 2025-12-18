package com.nianji.common.security.model;

import com.nianji.common.constant.SecurityConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 业务密钥对信息 包含完整的密钥信息，用于内部存储
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessKeyPair {

    /**
     * 业务标识
     */
    private String business;

    /**
     * 加密算法
     */
    private String algorithm;

    /**
     * 密钥版本
     */
    private String keyVersion;

    /**
     * RSA公钥
     */
    private String publicKey;

    /**
     * RSA私钥
     */
    private String privateKey;

    /**
     * AES对称密钥
     */
    private String symmetricKey;

    /**
     * 密钥创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedAt;

    /**
     * 是否有效
     */
    private boolean valid;

    /**
     * 使用次数
     */
    private long usageCount;

    /**
     * 转换为公钥信息（去除敏感信息）
     */
    public PublicKeyInfo toPublicKeyInfo() {
        PublicKeyInfo.PublicKeyInfoBuilder builder = PublicKeyInfo.builder()
                .algorithm(this.algorithm)
                .keyVersion(this.keyVersion)
                .business(this.business)
                .createdAt(this.createdAt)
                .expiresAt(this.expiresAt)
                .valid(this.valid);

        if (SecurityConstants.ALGORITHM_RSA.equals(this.algorithm)) {
            builder.publicKey(this.publicKey);
        } else if (SecurityConstants.ALGORITHM_AES.equals(this.algorithm)) {
            builder.key(this.symmetricKey);
        }

        return builder.build();
    }

    /**
     * 记录使用
     */
    public void recordUsage() {
        this.lastUsedAt = LocalDateTime.now();
        this.usageCount++;
    }

    /**
     * 验证密钥对是否完整
     */
    public boolean isValidKeyPair() {
        if (!valid) {
            return false;
        }

        if (SecurityConstants.ALGORITHM_RSA.equals(algorithm)) {
            return publicKey != null && !publicKey.trim().isEmpty() &&
                    privateKey != null && !privateKey.trim().isEmpty();
        } else if (SecurityConstants.ALGORITHM_AES.equals(algorithm)) {
            return symmetricKey != null && !symmetricKey.trim().isEmpty();
        }

        return false;
    }
}