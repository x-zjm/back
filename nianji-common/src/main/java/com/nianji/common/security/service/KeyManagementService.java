package com.nianji.common.security.service;

import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.ExceptionFactory;
import com.nianji.common.exception.system.CryptoException;
import com.nianji.common.security.config.EncryptionConfig;
import com.nianji.common.security.encryption.EncryptionService;
import com.nianji.common.security.encryption.EncryptionServiceFactory;
import com.nianji.common.security.enums.EncryptionAlgorithm;
import com.nianji.common.security.model.AlgorithmKeyPair;
import com.nianji.common.security.model.KeyVersionManager;
import com.nianji.common.security.model.PublicKeyInfo;
import com.nianji.common.utils.CacheUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 完整的密钥管理服务 支持多算法、多版本、自动轮换、业务隔离、监控统计
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeyManagementService {

    private final EncryptionConfig config;
    private final EncryptionServiceFactory encryptionServiceFactory;
    private final CacheUtil cacheUtil;

    // 核心存储结构
    private final Map<EncryptionAlgorithm, KeyVersionManager> algorithmKeyManagers = new ConcurrentHashMap<>();
    private final Map<String, KeyVersionManager> businessKeyManagers = new ConcurrentHashMap<>();

    // 状态控制
    private volatile boolean initialized = false;
    private volatile boolean rotationInProgress = false;

    // 监控统计
    private final ServiceMetrics serviceMetrics = new ServiceMetrics();

    // 常量配置
    private static final String DEFAULT_BUSINESS = "default";
    private static final int MAX_KEY_VERSIONS = 5;
    private static final int CACHE_TTL_HOURS = 2;
    private static final String PUBLIC_KEY_CACHE_PREFIX = "encryption:publicKey:";

    @PostConstruct
    public void initialize() {
        log.info("开始初始化密钥管理服务...");
        initializeKeyManagement();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (!initialized) {
            log.warn("密钥管理服务未完全初始化，重新初始化...");
            initializeKeyManagement();
        }
        log.info("密钥管理服务初始化状态: {}", initialized ? "成功" : "失败");
    }

    // ============ 初始化方法 ============

    /**
     * 初始化密钥管理
     */
    public synchronized void initializeKeyManagement() {
        if (!config.isEnabled()) {
            log.info("加密服务未启用，跳过初始化");
            return;
        }

        try {
            // 清理现有状态
            clearExistingState();

            // 验证配置参数
            validateConfigParameters();

            // 初始化算法密钥管理器
            initializeAlgorithmManagers();

            // 初始化业务密钥管理器
            initializeBusinessManagers();

            initialized = true;
            serviceMetrics.recordInitialization(true);

            log.info("密钥管理服务初始化完成 - 支持算法: {}, 默认算法: {}",
                    config.getAlgorithms().size(), config.getDefaultAlgorithm());

        } catch (Exception e) {
            log.error("密钥管理服务初始化失败", e);
            serviceMetrics.recordInitialization(false);
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_ERROR,
                    "密钥管理服务初始化失败", e);
        }
    }

    /**
     * 清理现有状态
     */
    private void clearExistingState() {
        algorithmKeyManagers.clear();
        businessKeyManagers.clear();
        serviceMetrics.reset();
        log.debug("已清理现有密钥状态");
    }

    /**
     * 验证配置参数（私有方法）
     */
    private void validateConfigParameters() {
        if (config.getAlgorithms() == null || config.getAlgorithms().isEmpty()) {
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_ERROR,
                    "未配置任何加密算法");
        }

        if (config.getDefaultAlgorithm() == null) {
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_ERROR,
                    "未配置默认算法");
        }

        if (!config.getAlgorithms().contains(config.getDefaultAlgorithm())) {
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_ERROR,
                    "默认算法不在支持的算法列表中");
        }

        log.debug("加密配置参数验证通过");
    }

    /**
     * 初始化算法密钥管理器
     */
    private void initializeAlgorithmManagers() {
        for (EncryptionAlgorithm algorithm : config.getAlgorithms()) {
            try {
                initializeAlgorithmManager(algorithm);
            } catch (Exception e) {
                log.error("算法密钥管理器初始化失败 - 算法: {}", algorithm, e);
                // 继续初始化其他算法
            }
        }
    }

    /**
     * 初始化单个算法管理器
     */
    private void initializeAlgorithmManager(EncryptionAlgorithm algorithm) {
        KeyVersionManager manager = new KeyVersionManager();
        manager.setBusiness(algorithm.name());

        // 创建初始密钥
        AlgorithmKeyPair initialKey = createKeyPair(algorithm, generateKeyVersion());
        manager.addKeyVersion(initialKey);
        manager.setCurrentVersion(initialKey.getKeyVersion());

        // 预生成下一个版本（如果启用自动轮换）
        if (config.isAutoRotation()) {
            AlgorithmKeyPair nextKey = createKeyPair(algorithm, generateKeyVersion());
            nextKey.setValid(false); // 暂不激活
            manager.addKeyVersion(nextKey);
            manager.setNextVersion(nextKey.getKeyVersion());
        }

        manager.setLastRotationTime(LocalDateTime.now());
        algorithmKeyManagers.put(algorithm, manager);

        // 缓存公钥信息
        cachePublicKeyInfo(algorithm, initialKey);

        log.info("算法密钥管理器初始化完成 - 算法: {}, 版本: {}",
                algorithm, initialKey.getKeyVersion());
    }

    /**
     * 初始化业务密钥管理器
     */
    private void initializeBusinessManagers() {
        // 初始化默认业务
        initializeBusinessManager(DEFAULT_BUSINESS);
    }

    private void initializeBusinessManager(String business) {
        KeyVersionManager manager = new KeyVersionManager();
        manager.setBusiness(business);

        // 使用默认算法的当前密钥
        EncryptionAlgorithm defaultAlgorithm = config.getDefaultAlgorithm();
        KeyVersionManager algoManager = algorithmKeyManagers.get(defaultAlgorithm);

        if (algoManager != null) {
            AlgorithmKeyPair currentKey = algoManager.getCurrentKey();
            manager.addKeyVersion(currentKey);
            manager.setCurrentVersion(currentKey.getKeyVersion());
            manager.setLastRotationTime(LocalDateTime.now());

            businessKeyManagers.put(business, manager);

            // 缓存业务公钥信息
            cacheBusinessPublicKeyInfo(business, currentKey);

            log.info("业务密钥管理器初始化完成 - 业务: {}, 算法: {}",
                    business, defaultAlgorithm);
        } else {
            log.warn("业务密钥管理器初始化失败 - 默认算法未初始化: {}", defaultAlgorithm);
        }
    }

    // ============ 密钥轮换管理 ============

    /**
     * 密钥轮换检查任务
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // 每30分钟检查一次
    public void scheduledKeyRotationCheck() {
        if (!initialized || !config.isAutoRotation() || rotationInProgress) {
            return;
        }

        log.debug("执行定时密钥轮换检查...");

        for (Map.Entry<EncryptionAlgorithm, KeyVersionManager> entry : algorithmKeyManagers.entrySet()) {
            EncryptionAlgorithm algorithm = entry.getKey();
            KeyVersionManager manager = entry.getValue();

            try {
                if (shouldRotateKey(manager)) {
                    log.info("触发密钥轮换 - 算法: {}", algorithm);
                    performKeyRotation(algorithm, manager);
                }
            } catch (Exception e) {
                log.error("密钥轮换检查失败 - 算法: {}", algorithm, e);
                serviceMetrics.recordRotationFailure(algorithm.name());
            }
        }
    }

    /**
     * 判断是否需要轮换密钥
     */
    private boolean shouldRotateKey(KeyVersionManager manager) {
        if (manager.getLastRotationTime() == null) {
            return true;
        }

        LocalDateTime nextRotation = manager.getLastRotationTime()
                .plusHours(config.getRotationIntervalHours());
        LocalDateTime advanceNotice = nextRotation.minusHours(1); // 提前1小时通知

        LocalDateTime now = LocalDateTime.now();

        // 需要轮换的情况：
        // 1. 已过轮换时间
        // 2. 接近轮换时间且有预备密钥
        return now.isAfter(nextRotation) ||
                (now.isAfter(advanceNotice) && manager.getNextVersion() != null);
    }

    /**
     * 执行密钥轮换
     */
    private synchronized void performKeyRotation(EncryptionAlgorithm algorithm, KeyVersionManager manager) {
        rotationInProgress = true;

        try {
            log.info("开始执行密钥轮换 - 算法: {}, 业务: {}", algorithm, manager.getBusiness());

            // 1. 激活预备密钥或生成新密钥
            if (manager.getNextVersion() != null) {
                activatePreparedKey(manager);
            } else {
                generateAndActivateNewKey(algorithm, manager);
            }

            // 2. 预生成下一个版本密钥
            prepareNextKeyVersion(algorithm, manager);

            // 3. 清理过期密钥版本
            cleanupExpiredKeys(manager);

            // 4. 更新缓存和状态
            updateAfterRotation(algorithm, manager);

            manager.setLastRotationTime(LocalDateTime.now());
            serviceMetrics.recordRotationSuccess(algorithm.name());

            log.info("密钥轮换完成 - 算法: {}, 新版本: {}", algorithm, manager.getCurrentVersion());

        } catch (Exception e) {
            log.error("密钥轮换执行失败 - 算法: {}", algorithm, e);
            serviceMetrics.recordRotationFailure(algorithm.name());
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_ERROR, "密钥轮换失败", e);
        } finally {
            rotationInProgress = false;
        }
    }

    /**
     * 激活预备密钥
     */
    private void activatePreparedKey(KeyVersionManager manager) {
        String nextVersion = manager.getNextVersion();
        AlgorithmKeyPair nextKey = manager.getKeyByVersion(nextVersion);

        // 标记当前密钥为即将过期
        AlgorithmKeyPair currentKey = manager.getCurrentKey();
        currentKey.setExpiresAt(LocalDateTime.now().plusHours(2));
        currentKey.setValid(true); // 仍然有效，用于解密历史数据

        // 激活预备密钥
        nextKey.setValid(true);
        nextKey.setLastUsedAt(LocalDateTime.now());
        manager.setCurrentVersion(nextVersion);
        manager.setNextVersion(null);

        log.debug("激活预备密钥: {} -> {}", currentKey.getKeyVersion(), nextVersion);
    }

    /**
     * 生成并激活新密钥
     */
    private void generateAndActivateNewKey(EncryptionAlgorithm algorithm, KeyVersionManager manager) {
        String newVersion = generateKeyVersion();
        AlgorithmKeyPair newKey = createKeyPair(algorithm, newVersion);

        // 标记当前密钥
        AlgorithmKeyPair currentKey = manager.getCurrentKey();
        currentKey.setExpiresAt(LocalDateTime.now().plusHours(2));

        // 添加并激活新密钥
        manager.addKeyVersion(newKey);
        manager.setCurrentVersion(newVersion);

        log.debug("生成并激活新密钥: {} -> {}", currentKey.getKeyVersion(), newVersion);
    }

    /**
     * 预备下一个版本密钥
     */
    private void prepareNextKeyVersion(EncryptionAlgorithm algorithm, KeyVersionManager manager) {
        String nextVersion = generateKeyVersion();
        AlgorithmKeyPair nextKey = createKeyPair(algorithm, nextVersion);
        nextKey.setValid(false); // 不激活，仅作为预备

        manager.addKeyVersion(nextKey);
        manager.setNextVersion(nextVersion);

        log.debug("预备下一个密钥版本: {}", nextVersion);
    }

    /**
     * 清理过期密钥
     */
    private void cleanupExpiredKeys(KeyVersionManager manager) {
        int beforeCleanup = manager.getKeyVersions().size();
        manager.cleanupExpiredKeys(MAX_KEY_VERSIONS);
        int afterCleanup = manager.getKeyVersions().size();

        if (beforeCleanup > afterCleanup) {
            log.debug("密钥清理完成: {} -> {}", beforeCleanup, afterCleanup);
        }
    }

    /**
     * 轮换后更新操作
     */
    private void updateAfterRotation(EncryptionAlgorithm algorithm, KeyVersionManager manager) {
        // 更新缓存
        AlgorithmKeyPair currentKey = manager.getCurrentKey();
        cachePublicKeyInfo(algorithm, currentKey);

        // 更新相关业务密钥管理器
        updateBusinessManagers(algorithm, currentKey);

        // 发布轮换事件
        publishKeyRotationEvent(algorithm, manager.getBusiness(), currentKey.getKeyVersion());
    }

    /**
     * 更新业务密钥管理器
     */
    private void updateBusinessManagers(EncryptionAlgorithm algorithm, AlgorithmKeyPair newKey) {
        for (KeyVersionManager businessManager : businessKeyManagers.values()) {
            // 如果业务使用这个算法，则更新
            AlgorithmKeyPair currentBusinessKey = businessManager.getCurrentKey();
            if (currentBusinessKey != null && currentBusinessKey.getAlgorithm() == algorithm) {
                businessManager.addKeyVersion(newKey);
                businessManager.setCurrentVersion(newKey.getKeyVersion());
                businessManager.setLastRotationTime(LocalDateTime.now());

                // 缓存业务公钥信息
                cacheBusinessPublicKeyInfo(businessManager.getBusiness(), newKey);
            }
        }
    }

    // ============ 公开业务方法 ============

    /**
     * 获取当前公钥信息
     */
    public PublicKeyInfo getPublicKeyInfo(EncryptionAlgorithm algorithm) {
        checkInitialized();

        KeyVersionManager manager = getAlgorithmManager(algorithm);
        AlgorithmKeyPair currentKey = manager.getCurrentKey();

        currentKey.recordUsage();
        serviceMetrics.recordPublicKeyAccess(algorithm.name());

        return currentKey.toPublicKeyInfo();
    }

    /**
     * 获取默认算法公钥信息
     */
    public PublicKeyInfo getPublicKeyInfo() {
        return getPublicKeyInfo(config.getDefaultAlgorithm());
    }

    /**
     * 获取业务公钥信息
     */
    public PublicKeyInfo getBusinessPublicKeyInfo(String business) {
        checkInitialized();

        KeyVersionManager manager = businessKeyManagers.get(business);
        if (manager == null) {
            log.warn("业务密钥管理器不存在，使用默认业务: {}", business);
            manager = businessKeyManagers.get(DEFAULT_BUSINESS);
        }

        AlgorithmKeyPair currentKey = manager.getCurrentKey();
        currentKey.recordUsage();
        serviceMetrics.recordBusinessAccess(business);

        return currentKey.toPublicKeyInfo();
    }

    /**
     * 获取指定版本公钥信息
     */
    public PublicKeyInfo getPublicKeyInfoByVersion(EncryptionAlgorithm algorithm, String version) {
        checkInitialized();

        KeyVersionManager manager = getAlgorithmManager(algorithm);
        AlgorithmKeyPair key = manager.getKeyByVersion(version);

        if (!key.isValid() && isKeyExpired(key)) {
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_ERROR, "密钥版本已过期: " + version);
        }

        key.recordUsage();
        serviceMetrics.recordVersionedAccess(algorithm.name());

        return key.toPublicKeyInfo();
    }

    /**
     * 解密数据（自动版本识别）
     */
    public String decryptAuto(EncryptionAlgorithm algorithm, String encryptedData) {
        checkInitialized();

        KeyVersionManager manager = getAlgorithmManager(algorithm);

        // 修复：创建可修改的列表副本
        List<AlgorithmKeyPair> validKeys = new ArrayList<>(manager.getValidKeyVersions());

        // 按使用时间倒序尝试（最近使用的优先）
        validKeys.sort((k1, k2) -> {
            LocalDateTime t1 = k1.getLastUsedAt() != null ? k1.getLastUsedAt() : k1.getCreatedAt();
            LocalDateTime t2 = k2.getLastUsedAt() != null ? k2.getLastUsedAt() : k2.getCreatedAt();
            return t2.compareTo(t1);
        });

        Exception lastException = null;

        for (AlgorithmKeyPair key : validKeys) {
            try {
                String decrypted = performDecryption(algorithm, encryptedData, key);

                key.recordUsage();
                serviceMetrics.recordDecryptionSuccess(algorithm.name(), key.getKeyVersion());

                log.debug("解密成功 - 算法: {}, 版本: {}", algorithm, key.getKeyVersion());
                return decrypted;

            } catch (Exception e) {
                lastException = e;
                serviceMetrics.recordDecryptionFailure(algorithm.name(), key.getKeyVersion());
                log.debug("解密尝试失败 - 算法: {}, 版本: {}", algorithm, key.getKeyVersion());
            }
        }

        serviceMetrics.recordDecryptionFailure(algorithm.name(), "all");
        log.error("所有密钥版本解密均失败 - 算法: {}, 尝试版本数: {}", algorithm, validKeys.size());

        throw ExceptionFactory.crypto(ErrorCode.System.DECRYPT_FAILED,
                "解密失败，请检查加密数据或密钥版本", lastException);
    }

    /**
     * 使用指定版本解密
     */
    public String decryptWithVersion(EncryptionAlgorithm algorithm, String version, String encryptedData) {
        checkInitialized();

        KeyVersionManager manager = getAlgorithmManager(algorithm);
        AlgorithmKeyPair key = manager.getKeyByVersion(version);

        if (!key.isValid() && isKeyExpired(key)) {
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_ERROR, "密钥版本已过期: " + version);
        }

        try {
            String decrypted = performDecryption(algorithm, encryptedData, key);

            key.recordUsage();
            serviceMetrics.recordDecryptionSuccess(algorithm.name(), version);

            return decrypted;

        } catch (Exception e) {
            serviceMetrics.recordDecryptionFailure(algorithm.name(), version);

            log.error("指定版本解密失败 - 算法: {}, 版本: {}", algorithm, version, e);
            throw ExceptionFactory.crypto(ErrorCode.System.DECRYPT_FAILED,
                    "指定版本解密失败: " + version, e);
        }
    }

    /**
     * 获取私钥（RSA算法）
     */
    public String getPrivateKey(EncryptionAlgorithm algorithm) {
        checkInitialized();

        if (!algorithm.name().startsWith("RSA")) {
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_ERROR,
                    "当前算法不支持私钥操作: " + algorithm);
        }

        KeyVersionManager manager = getAlgorithmManager(algorithm);
        AlgorithmKeyPair currentKey = manager.getCurrentKey();
        currentKey.recordUsage();

        return currentKey.getPrivateKey();
    }

    /**
     * 获取对称密钥（AES算法）
     */
    public String getSymmetricKey(EncryptionAlgorithm algorithm) {
        checkInitialized();

        if (!algorithm.name().startsWith("AES")) {
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_ERROR,
                    "当前算法不是对称加密: " + algorithm);
        }

        KeyVersionManager manager = getAlgorithmManager(algorithm);
        AlgorithmKeyPair currentKey = manager.getCurrentKey();
        currentKey.recordUsage();

        return currentKey.getSymmetricKey();
    }

    /**
     * 获取支持的算法列表
     */
    public Set<EncryptionAlgorithm> getSupportedAlgorithms() {
        return new HashSet<>(config.getAlgorithms());
    }

    /**
     * 手动触发密钥轮换
     */
    public void manualRotateKey(EncryptionAlgorithm algorithm) {
        checkInitialized();

        KeyVersionManager manager = getAlgorithmManager(algorithm);
        performKeyRotation(algorithm, manager);

        serviceMetrics.recordManualRotation(algorithm.name());
        log.info("手动密钥轮换完成 - 算法: {}", algorithm);
    }

    /**
     * 重新初始化密钥服务
     */
    public synchronized void reinitialize() {
        log.info("手动触发密钥服务重新初始化...");
        initializeKeyManagement();
        serviceMetrics.recordReinitialization();
    }

    // ============ 状态查询和管理方法 ============

    /**
     * 获取服务状态
     */
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new LinkedHashMap<>();

        status.put("initialized", initialized);
        status.put("rotationInProgress", rotationInProgress);
        status.put("enabled", config.isEnabled());
        status.put("autoRotation", config.isAutoRotation());
        status.put("rotationIntervalHours", config.getRotationIntervalHours());
        status.put("supportedAlgorithms", config.getAlgorithms().size());
        status.put("defaultAlgorithm", config.getDefaultAlgorithm().name());

        // 算法状态
        Map<String, Object> algorithmStatus = new LinkedHashMap<>();
        for (Map.Entry<EncryptionAlgorithm, KeyVersionManager> entry : algorithmKeyManagers.entrySet()) {
            algorithmStatus.put(entry.getKey().name(), getAlgorithmStatus(entry.getValue()));
        }
        status.put("algorithms", algorithmStatus);

        // 业务状态
        Map<String, Object> businessStatus = new LinkedHashMap<>();
        for (Map.Entry<String, KeyVersionManager> entry : businessKeyManagers.entrySet()) {
            businessStatus.put(entry.getKey(), getAlgorithmStatus(entry.getValue()));
        }
        status.put("businesses", businessStatus);

        // 监控指标
        status.put("metrics", serviceMetrics.getSummary());

        return status;
    }

    /**
     * 检查服务是否就绪
     */
    public boolean isReady() {
        return initialized && !algorithmKeyManagers.isEmpty() && !businessKeyManagers.isEmpty();
    }

    /**
     * 验证配置（公开方法）- 修正后的单一方法
     */
    public boolean validateConfiguration() {
        try {
            // 验证基本配置参数
            if (!config.isEnabled()) {
                return false;
            }

            if (config.getAlgorithms() == null || config.getAlgorithms().isEmpty()) {
                return false;
            }

            if (config.getDefaultAlgorithm() == null) {
                return false;
            }

            if (!config.getAlgorithms().contains(config.getDefaultAlgorithm())) {
                return false;
            }

            // 验证服务状态
            return initialized && isReady();

        } catch (Exception e) {
            log.error("配置验证失败", e);
            return false;
        }
    }

    /**
     * 获取算法名称
     */
    public String getAlgorithm() {
        return config.getDefaultAlgorithm().name();
    }

    // ============ 私有工具方法 ============

    /**
     * 创建密钥对
     */
    private AlgorithmKeyPair createKeyPair(EncryptionAlgorithm algorithm, String version) {
        AlgorithmKeyPair.AlgorithmKeyPairBuilder builder = AlgorithmKeyPair.builder()
                .algorithm(algorithm)
                .keyVersion(version)
                .createdAt(LocalDateTime.now())
                .lastUsedAt(LocalDateTime.now())
                .valid(true)
                .usageCount(0L)
                .expiresAt(LocalDateTime.now().plusHours(config.getRotationIntervalHours() + 2));

        // 根据算法类型设置密钥
        if (algorithm.name().startsWith("RSA")) {
            setupRsaKeys(algorithm, builder);
        } else if (algorithm.name().startsWith("AES")) {
            setupAesKeys(algorithm, builder);
        } else {
            // 哈希算法等不需要密钥
            log.debug("跳过密钥生成 - 算法类型: {}", algorithm);
        }

        return builder.build();
    }

    /**
     * 设置RSA密钥
     */
    private void setupRsaKeys(EncryptionAlgorithm algorithm, AlgorithmKeyPair.AlgorithmKeyPairBuilder builder) {
        try {
            // 优先使用配置的密钥
            if (StringUtils.hasText(config.getRsaPublicKey()) && StringUtils.hasText(config.getRsaPrivateKey())) {
                builder.publicKey(config.getRsaPublicKey())
                        .privateKey(config.getRsaPrivateKey());
                log.debug("使用配置的RSA密钥 - 算法: {}", algorithm);
            } else {
                // 自动生成密钥对
                com.nianji.common.security.encryption.impl.RsaEncryptionService rsaService =
                        (com.nianji.common.security.encryption.impl.RsaEncryptionService)
                                encryptionServiceFactory.getService(algorithm);

                java.security.KeyPair keyPair = rsaService.generateKeyPair();
                String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
                String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

                builder.publicKey(publicKey).privateKey(privateKey);
                log.warn("自动生成RSA密钥对 - 算法: {}", algorithm);
            }
        } catch (Exception e) {
            log.error("RSA密钥设置失败 - 算法: {}", algorithm, e);
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_GENERATE_FAILED,
                    "RSA密钥生成失败", e);
        }
    }

    /**
     * 设置AES密钥
     */
    private void setupAesKeys(EncryptionAlgorithm algorithm, AlgorithmKeyPair.AlgorithmKeyPairBuilder builder) {
        try {
            // 优先使用配置的密钥
            if (StringUtils.hasText(config.getAesKey())) {
                builder.symmetricKey(config.getAesKey());
                log.debug("使用配置的AES密钥 - 算法: {}", algorithm);
            } else {
                // 自动生成密钥
                EncryptionService encryptionService = encryptionServiceFactory.getService(algorithm);
                String generatedKey = encryptionService.generateKey();
                builder.symmetricKey(generatedKey);
                log.warn("自动生成AES密钥 - 算法: {}", algorithm);
            }
        } catch (Exception e) {
            log.error("AES密钥设置失败 - 算法: {}", algorithm, e);
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_GENERATE_FAILED,
                    "AES密钥生成失败", e);
        }
    }

    /**
     * 执行解密操作
     */
    private String performDecryption(EncryptionAlgorithm algorithm, String encryptedData, AlgorithmKeyPair key) {
        EncryptionService encryptionService = encryptionServiceFactory.getService(algorithm);

        if (algorithm.name().startsWith("RSA")) {
            return encryptionService.decrypt(encryptedData, key.getPrivateKey());
        } else if (algorithm.name().startsWith("AES")) {
            return encryptionService.decrypt(encryptedData, key.getSymmetricKey());
        } else {
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_ERROR,
                    "不支持的解密算法: " + algorithm);
        }
    }

    /**
     * 缓存公钥信息
     */
    private void cachePublicKeyInfo(EncryptionAlgorithm algorithm, AlgorithmKeyPair key) {
        try {
            String cacheKey = PUBLIC_KEY_CACHE_PREFIX + "algorithm:" + algorithm.name();
            PublicKeyInfo publicKeyInfo = key.toPublicKeyInfo();

            cacheUtil.put(cacheKey, publicKeyInfo, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.debug("算法公钥信息已缓存 - 算法: {}, 版本: {}", algorithm, key.getKeyVersion());

        } catch (Exception e) {
            log.error("缓存公钥信息失败 - 算法: {}", algorithm, e);
        }
    }

    /**
     * 缓存业务公钥信息
     */
    private void cacheBusinessPublicKeyInfo(String business, AlgorithmKeyPair key) {
        try {
            String cacheKey = PUBLIC_KEY_CACHE_PREFIX + "business:" + business;
            PublicKeyInfo publicKeyInfo = key.toPublicKeyInfo();

            cacheUtil.put(cacheKey, publicKeyInfo, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.debug("业务公钥信息已缓存 - 业务: {}, 版本: {}", business, key.getKeyVersion());

        } catch (Exception e) {
            log.error("缓存业务公钥信息失败 - 业务: {}", business, e);
        }
    }

    /**
     * 发布密钥轮换事件
     */
    private void publishKeyRotationEvent(EncryptionAlgorithm algorithm, String business, String newVersion) {
        // 这里可以集成消息队列、事件总线等
        // 用于通知相关服务密钥已更新
        log.info("发布密钥轮换事件 - 算法: {}, 业务: {}, 新版本: {}", algorithm, business, newVersion);
    }

    /**
     * 生成版本号
     */
    private String generateKeyVersion() {
        return "v" + UUID.randomUUID().toString().substring(0, 8) +
                "-" + System.currentTimeMillis();
    }

    /**
     * 检查密钥是否过期
     */
    private boolean isKeyExpired(AlgorithmKeyPair key) {
        return key.getExpiresAt() != null &&
                key.getExpiresAt().isBefore(LocalDateTime.now());
    }

    /**
     * 检查服务是否已初始化
     */
    private void checkInitialized() {
        if (!initialized) {
            log.warn("密钥服务未初始化，尝试立即初始化...");
            initializeKeyManagement();

            if (!initialized) {
                throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_ERROR, "密钥服务未初始化");
            }
        }
    }

    /**
     * 获取算法管理器
     */
    private KeyVersionManager getAlgorithmManager(EncryptionAlgorithm algorithm) {
        KeyVersionManager manager = algorithmKeyManagers.get(algorithm);
        if (manager == null) {
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_ERROR,
                    "算法未支持: " + algorithm);
        }
        return manager;
    }

    /**
     * 获取算法状态
     */
    private Map<String, Object> getAlgorithmStatus(KeyVersionManager manager) {
        Map<String, Object> status = new LinkedHashMap<>();

        AlgorithmKeyPair currentKey = manager.getCurrentKey();
        if (currentKey != null) {
            status.put("currentVersion", currentKey.getKeyVersion());
            status.put("algorithm", currentKey.getAlgorithm().name());
            status.put("createdAt", currentKey.getCreatedAt());
            status.put("lastUsedAt", currentKey.getLastUsedAt());
            status.put("usageCount", currentKey.getUsageCount());
            status.put("valid", currentKey.isValid());
        }

        status.put("nextVersion", manager.getNextVersion());
        status.put("lastRotation", manager.getLastRotationTime());
        status.put("totalVersions", manager.getKeyVersions().size());
        status.put("validVersions", manager.getValidKeyVersions().size());

        return status;
    }

    // ============ 内部监控类 ============

    /**
     * 服务监控指标
     */
    private static class ServiceMetrics {
        private final AtomicLong totalOperations = new AtomicLong(0);
        private final AtomicLong successOperations = new AtomicLong(0);
        private final Map<String, AtomicLong> algorithmMetrics = new ConcurrentHashMap<>();
        private final Map<String, AtomicLong> businessMetrics = new ConcurrentHashMap<>();
        private final Map<String, AtomicLong> operationMetrics = new ConcurrentHashMap<>();

        private long startTime = System.currentTimeMillis();
        private long lastInitializationTime = 0;
        private boolean lastInitializationSuccess = false;

        public void recordInitialization(boolean success) {
            lastInitializationTime = System.currentTimeMillis();
            lastInitializationSuccess = success;
            recordOperation("initialization", success);
        }

        public void recordRotationSuccess(String algorithm) {
            recordOperation("rotation.success", true);
            algorithmMetrics.computeIfAbsent(algorithm, k -> new AtomicLong(0)).incrementAndGet();
        }

        public void recordRotationFailure(String algorithm) {
            recordOperation("rotation.failure", false);
        }

        public void recordManualRotation(String algorithm) {
            recordOperation("manual.rotation", true);
        }

        public void recordReinitialization() {
            recordOperation("reinitialization", true);
        }

        public void recordPublicKeyAccess(String algorithm) {
            recordOperation("publicKey.access", true);
            algorithmMetrics.computeIfAbsent(algorithm, k -> new AtomicLong(0)).incrementAndGet();
        }

        public void recordBusinessAccess(String business) {
            recordOperation("business.access", true);
            businessMetrics.computeIfAbsent(business, k -> new AtomicLong(0)).incrementAndGet();
        }

        public void recordVersionedAccess(String algorithm) {
            recordOperation("versioned.access", true);
        }

        public void recordDecryptionSuccess(String algorithm, String version) {
            recordOperation("decryption.success", true);
            algorithmMetrics.computeIfAbsent(algorithm, k -> new AtomicLong(0)).incrementAndGet();
        }

        public void recordDecryptionFailure(String algorithm, String version) {
            recordOperation("decryption.failure", false);
        }

        private void recordOperation(String operation, boolean success) {
            totalOperations.incrementAndGet();
            if (success) {
                successOperations.incrementAndGet();
            }
            operationMetrics.computeIfAbsent(operation, k -> new AtomicLong(0)).incrementAndGet();
        }

        public void reset() {
            totalOperations.set(0);
            successOperations.set(0);
            algorithmMetrics.clear();
            businessMetrics.clear();
            operationMetrics.clear();
            startTime = System.currentTimeMillis();
        }

        public Map<String, Object> getSummary() {
            Map<String, Object> summary = new LinkedHashMap<>();

            long total = totalOperations.get();
            long success = successOperations.get();
            double successRate = total > 0 ? (double) success / total * 100 : 0.0;

            summary.put("totalOperations", total);
            summary.put("successOperations", success);
            summary.put("failureOperations", total - success);
            summary.put("successRate", String.format("%.2f%%", successRate));
            summary.put("uptime", System.currentTimeMillis() - startTime);
            summary.put("lastInitializationTime", lastInitializationTime);
            summary.put("lastInitializationSuccess", lastInitializationSuccess);

            // 算法使用统计
            Map<String, Object> algorithmStats = new LinkedHashMap<>();
            algorithmMetrics.forEach((algo, count) ->
                    algorithmStats.put(algo, count.get()));
            summary.put("algorithmUsage", algorithmStats);

            // 业务使用统计
            Map<String, Object> businessStats = new LinkedHashMap<>();
            businessMetrics.forEach((business, count) ->
                    businessStats.put(business, count.get()));
            summary.put("businessUsage", businessStats);

            // 操作统计
            Map<String, Object> operationStats = new LinkedHashMap<>();
            operationMetrics.forEach((op, count) ->
                    operationStats.put(op, count.get()));
            summary.put("operationStats", operationStats);

            return summary;
        }
    }
}