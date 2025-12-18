package com.nianji.common.security.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 密钥版本管理
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyVersionManager {

    /**
     * 业务标识
     */
    private String business;

    /**
     * 所有密钥版本（按创建时间排序）
     */
    private List<AlgorithmKeyPair> keyVersions = new CopyOnWriteArrayList<>();

    /**
     * 当前激活的密钥版本
     */
    private String currentVersion;

    /**
     * 下一个预备密钥版本
     */
    private String nextVersion;

    /**
     * 最后轮换时间
     */
    private LocalDateTime lastRotationTime;

    /**
     * 添加密钥版本
     */
    public synchronized void addKeyVersion(AlgorithmKeyPair keyPair) {
        keyVersions.add(keyPair);
        // 按创建时间排序
        keyVersions.sort(Comparator.comparing(AlgorithmKeyPair::getCreatedAt).reversed());
    }

    /**
     * 获取当前激活的密钥
     */
    public AlgorithmKeyPair getCurrentKey() {
        return keyVersions.stream()
                .filter(k -> k.getKeyVersion().equals(currentVersion))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("当前激活密钥不存在: " + currentVersion));
    }

    /**
     * 获取指定版本的密钥
     */
    public AlgorithmKeyPair getKeyByVersion(String version) {
        return keyVersions.stream()
                .filter(k -> k.getKeyVersion().equals(version) && k.isValid())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("密钥版本不存在或已失效: " + version));
    }

    /**
     * 获取所有有效密钥版本
     */
    public List<AlgorithmKeyPair> getValidKeyVersions() {
        return keyVersions.stream()
                .filter(AlgorithmKeyPair::isValid)
                .collect(Collectors.toList());
    }

    /**
     * 标记密钥版本为过期
     */
    public synchronized void markVersionExpired(String version) {
        keyVersions.stream()
                .filter(k -> k.getKeyVersion().equals(version))
                .findFirst()
                .ifPresent(key -> key.setValid(false));
    }

    /**
     * 清理过期密钥（保留最近N个版本）
     */
    public synchronized void cleanupExpiredKeys(int keepVersions) {
        if (keyVersions.size() <= keepVersions) {
            return;
        }

        List<AlgorithmKeyPair> sorted = keyVersions.stream()
                .sorted(Comparator.comparing(AlgorithmKeyPair::getCreatedAt).reversed())
                .toList();

        for (int i = keepVersions; i < sorted.size(); i++) {
            AlgorithmKeyPair key = sorted.get(i);
            keyVersions.remove(key);
        }
    }
}