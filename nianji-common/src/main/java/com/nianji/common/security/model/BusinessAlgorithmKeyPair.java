package com.nianji.common.security.model;

import com.nianji.common.security.enums.EncryptionAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 业务多算法密钥对
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessAlgorithmKeyPair {
    
    /**
     * 业务标识
     */
    private String business;
    
    /**
     * 算法 -> 密钥对映射
     */
    private Map<EncryptionAlgorithm, AlgorithmKeyPair> algorithmKeyPairs;
    
    /**
     * 默认算法
     */
    private EncryptionAlgorithm defaultAlgorithm;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedAt;
    
    /**
     * 是否有效
     */
    private boolean valid;
    
    public BusinessAlgorithmKeyPair(String business) {
        this.business = business;
        this.algorithmKeyPairs = new ConcurrentHashMap<>();
        this.createdAt = LocalDateTime.now();
        this.lastUsedAt = LocalDateTime.now();
        this.valid = true;
    }
    
    public void addAlgorithmKeyPair(EncryptionAlgorithm algorithm, AlgorithmKeyPair keyPair) {
        algorithmKeyPairs.put(algorithm, keyPair);
    }
    
    public AlgorithmKeyPair getAlgorithmKeyPair(EncryptionAlgorithm algorithm) {
        return algorithmKeyPairs.get(algorithm);
    }
    
    public Set<EncryptionAlgorithm> getSupportedAlgorithms() {
        return algorithmKeyPairs.keySet();
    }
    
    public PublicKeyInfo getDefaultPublicKeyInfo() {
        if (defaultAlgorithm == null) {
            return null;
        }
        AlgorithmKeyPair keyPair = algorithmKeyPairs.get(defaultAlgorithm);
        return keyPair != null ? keyPair.toPublicKeyInfo() : null;
    }
    
    public void recordDefaultAlgorithmUsage() {
        this.lastUsedAt = LocalDateTime.now();
        if (defaultAlgorithm != null) {
            AlgorithmKeyPair keyPair = algorithmKeyPairs.get(defaultAlgorithm);
            if (keyPair != null) {
                keyPair.recordUsage();
            }
        }
    }
    
    public boolean isValid() {
        return valid && algorithmKeyPairs.values().stream()
                .anyMatch(AlgorithmKeyPair::isValid);
    }
}