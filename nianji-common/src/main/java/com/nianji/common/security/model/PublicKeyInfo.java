package com.nianji.common.security.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 公钥信息数据传输对象
 * 放在公共模块，供所有模块使用
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicKeyInfo {
    
    /**
     * 加密算法
     */
    private String algorithm;
    
    /**
     * 密钥版本
     */
    private String keyVersion;
    
    /**
     * 业务标识
     */
    private String business;
    
    /**
     * 公钥（Base64编码）
     */
    private String publicKey;
    
    /**
     * 对称密钥（Base64编码，仅对称加密时使用）
     */
    private String key;
    
    /**
     * 密钥创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;
    
    /**
     * 是否有效
     */
    private boolean valid;
}