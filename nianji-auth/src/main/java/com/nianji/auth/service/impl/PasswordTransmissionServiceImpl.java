package com.nianji.auth.service.impl;

import com.nianji.auth.service.PasswordTransmissionService;
import com.nianji.common.exception.ExceptionFactory;
import com.nianji.common.exception.system.CryptoException;
import com.nianji.common.reqres.BizResult;
import com.nianji.common.security.model.PublicKeyInfo;
import com.nianji.common.security.service.KeyManagementService;
import com.nianji.common.security.encryption.EncryptionService;
import com.nianji.common.security.encryption.EncryptionServiceFactory;
import com.nianji.common.security.enums.EncryptionAlgorithm;
import com.nianji.common.errorcode.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 密码传输服务实现 负责处理密码的加密传输和解密验证
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordTransmissionServiceImpl implements PasswordTransmissionService {

    private final KeyManagementService keyManagementService;
    private final EncryptionServiceFactory encryptionServiceFactory;

    // 统计信息
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicLong lastErrorTime = new AtomicLong(0);

    // 业务标识
    private static final String AUTH_BUSINESS = "auth";

    // 默认加密算法
    private static final EncryptionAlgorithm DEFAULT_ALGORITHM = EncryptionAlgorithm.RSA_ECB_OAEP;

    // 算法优先级
    private static final EncryptionAlgorithm[] ALGORITHM_PRIORITY = {
            EncryptionAlgorithm.RSA_ECB_OAEP,
            EncryptionAlgorithm.AES_GCM,
            EncryptionAlgorithm.RSA_ECB_PKCS1,
            EncryptionAlgorithm.AES_CBC_PKCS5
    };

    // 缓存最近使用的算法（用于优化自动探测）
    private final Map<String, EncryptionAlgorithm> algorithmCache = new ConcurrentHashMap<>();

    /**
     * 使用指定算法解密前端传输的加密密码
     */
    @Override
    public String decryptPassword(String encryptedPassword, EncryptionAlgorithm algorithm) {
        long startTime = System.currentTimeMillis();
        totalRequests.incrementAndGet();

        // 前置验证
        validateEncryptedData(encryptedPassword);

        try {
            String result = decryptPasswordInternal(encryptedPassword, algorithm);

            // 记录成功指标
            successRequests.incrementAndGet();
            long duration = System.currentTimeMillis() - startTime;

            log.debug("密码解密成功 - 算法: {}, 耗时: {}ms", algorithm, duration);
            return result;

        } catch (Exception e) {
            // 记录失败指标
            failedRequests.incrementAndGet();
            lastErrorTime.set(System.currentTimeMillis());

            log.error("密码解密失败 - 算法: {}", algorithm, e);
            throw ExceptionFactory.crypto(ErrorCode.System.DECRYPT_FAILED,
                    "密码解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 自动探测算法并解密前端传输的加密密码
     */
    @Override
    public String decryptPassword(String encryptedPassword) {
        long startTime = System.currentTimeMillis();
        totalRequests.incrementAndGet();

        // 前置验证
        validateEncryptedData(encryptedPassword);

        // 首先尝试缓存的算法（优化性能）
        String cacheKey = getEncryptedDataKey(encryptedPassword);
        EncryptionAlgorithm cachedAlgorithm = algorithmCache.get(cacheKey);
        if (cachedAlgorithm != null) {
            try {
                String result = decryptPasswordInternal(encryptedPassword, cachedAlgorithm);
                successRequests.incrementAndGet();
                log.debug("使用缓存算法解密成功 - 算法: {}, 耗时: {}ms",
                        cachedAlgorithm, System.currentTimeMillis() - startTime);
                return result;
            } catch (Exception e) {
                log.debug("缓存算法解密失败，尝试其他算法: {}", cachedAlgorithm);
                // 继续尝试其他算法
            }
        }

        // 获取支持的算法
        Set<EncryptionAlgorithm> supportedAlgorithms = keyManagementService.getSupportedAlgorithms();

        // 按优先级尝试算法
        Exception lastException = null;
        for (EncryptionAlgorithm algorithm : ALGORITHM_PRIORITY) {
            if (supportedAlgorithms.contains(algorithm)) {
                try {
                    String result = decryptPasswordInternal(encryptedPassword, algorithm);

                    // 记录算法选择到缓存（优化下次选择）
                    algorithmCache.put(cacheKey, algorithm);

                    // 记录成功指标
                    successRequests.incrementAndGet();
                    long duration = System.currentTimeMillis() - startTime;

                    log.debug("密码解密成功 - 算法: {}, 耗时: {}ms", algorithm, duration);
                    return result;

                } catch (CryptoException e) {
                    lastException = e;
                    log.debug("算法 {} 解密失败，尝试下一种算法: {}", algorithm, e.getMessage());
                }
            }
        }

        // 记录失败指标
        failedRequests.incrementAndGet();
        lastErrorTime.set(System.currentTimeMillis());

        log.error("所有算法解密均失败 - 尝试算法数: {}, 数据长度: {}",
                ALGORITHM_PRIORITY.length, encryptedPassword.length());
        throw ExceptionFactory.crypto(ErrorCode.System.DECRYPT_FAILED,
                "所有算法解密均失败", lastException);
    }

    /**
     * 获取当前用于前端加密的公钥信息
     */
    @Override
    public PublicKeyInfo getPublicKeyInfo() {
        long startTime = System.currentTimeMillis();
        totalRequests.incrementAndGet();

        try {
            PublicKeyInfo publicKeyInfo = keyManagementService.getBusinessPublicKeyInfo(AUTH_BUSINESS);

            // 记录成功指标
            successRequests.incrementAndGet();
            long duration = System.currentTimeMillis() - startTime;

            log.debug("获取公钥信息成功 - 业务: {}, 算法: {}, 版本: {}, 耗时: {}ms",
                    AUTH_BUSINESS,
                    publicKeyInfo.getAlgorithm(),
                    publicKeyInfo.getKeyVersion(),
                    duration);

            return publicKeyInfo;

        } catch (Exception e) {
            // 记录失败指标
            failedRequests.incrementAndGet();
            lastErrorTime.set(System.currentTimeMillis());

            log.error("获取公钥信息失败 - 业务: {}", AUTH_BUSINESS, e);
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_ERROR,
                    "获取公钥信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证加密数据的格式是否正确
     */
    @Override
    public boolean validateEncryptedData(String encryptedData) {
        if (!StringUtils.hasText(encryptedData)) {
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_ERROR, "加密数据不能为空");
        }

        // 基本长度验证
        if (encryptedData.length() < 50) { // 合理的Base64最小长度
            log.warn("加密数据长度过短: {}", encryptedData.length());
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_ERROR, "加密数据长度异常");
        }

        if (encryptedData.length() > 2048) { // 合理的最大长度
            log.warn("加密数据长度过长: {}", encryptedData.length());
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_ERROR, "加密数据长度超出限制");
        }

        try {
            // Base64 格式验证
            java.util.Base64.getDecoder().decode(encryptedData);
            return true;
        } catch (Exception e) {
            log.warn("加密数据格式错误 - 不是有效的Base64编码");
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_ERROR, "加密数据格式错误");
        }
    }

    /**
     * 获取当前加密配置信息（用于日志记录和调试）
     */
    @Override
    public String getCurrentEncryptionInfo() {
        try {
            PublicKeyInfo publicKeyInfo = getPublicKeyInfo();
            if (publicKeyInfo != null) {
                return String.format("算法: %s, 版本: %s, 业务: %s, 状态: %s",
                        publicKeyInfo.getAlgorithm(),
                        publicKeyInfo.getKeyVersion(),
                        AUTH_BUSINESS,
                        publicKeyInfo.isValid() ? "有效" : "无效");
            }
            return "未知加密信息";
        } catch (Exception e) {
            log.warn("获取加密信息失败", e);
            return "加密信息获取失败: " + e.getMessage();
        }
    }

    /**
     * 加密服务健康检查
     */
    @Override
    public String healthCheck(String data) {
        long startTime = System.currentTimeMillis();

        try {
            // 验证配置
            if (!keyManagementService.validateConfiguration()) {
                return "FAILED: 加密配置无效";
            }

            // 验证服务就绪状态
            if (!keyManagementService.isReady()) {
                return "FAILED: 密钥服务未就绪";
            }

            // 测试所有支持的算法
            Set<EncryptionAlgorithm> supportedAlgorithms = keyManagementService.getSupportedAlgorithms();
            if (supportedAlgorithms.isEmpty()) {
                return "FAILED: 无支持的加密算法";
            }

            StringBuilder result = new StringBuilder();
            result.append("健康检查结果:\n");
            result.append(String.format("服务状态: %s\n", keyManagementService.isReady() ? "就绪" : "未就绪"));
            result.append(String.format("支持算法: %d种\n", supportedAlgorithms.size()));
            result.append(String.format("总请求数: %d\n", totalRequests.get()));
            result.append(String.format("成功率: %s\n", calculateSuccessRate()));

            int successTests = 0;
            int totalTests = 0;

            // 对每个支持的算法进行测试
            for (EncryptionAlgorithm algorithm : supportedAlgorithms) {
                totalTests++;
                try {
                    if (StringUtils.hasText(data)) {
                        // 如果有测试数据，执行完整的加密解密测试
                        String encrypted = encryptTestData(data, algorithm);
                        String decrypted = decryptPasswordInternal(encrypted, algorithm);

                        if (data.equals(decrypted)) {
                            successTests++;
                            result.append(String.format("✓ %s: 测试通过\n", algorithm));
                        } else {
                            result.append(String.format("✗ %s: 解密结果不匹配\n", algorithm));
                        }
                    } else {
                        // 如果没有测试数据，只测试基本功能
                        PublicKeyInfo publicKeyInfo = getPublicKeyInfo(algorithm);
                        if (publicKeyInfo != null && publicKeyInfo.isValid()) {
                            successTests++;
                            result.append(String.format("✓ %s: 基础功能正常\n", algorithm));
                        } else {
                            result.append(String.format("✗ %s: 公钥无效\n", algorithm));
                        }
                    }
                } catch (Exception e) {
                    result.append(String.format("✗ %s: %s\n", algorithm, e.getMessage()));
                    log.warn("健康检查异常 - 算法: {}", algorithm, e);
                }
            }

            result.append(String.format("测试通过率: %d/%d\n", successTests, totalTests));
            result.append(String.format("总耗时: %dms", System.currentTimeMillis() - startTime));

            log.info("密码传输服务健康检查完成 - 通过率: {}/{}, 耗时: {}ms",
                    successTests, totalTests, System.currentTimeMillis() - startTime);

            return result.toString();

        } catch (Exception e) {
            log.error("加密服务健康检查异常", e);
            return "FAILED: 健康检查异常 - " + e.getMessage();
        }
    }

    /**
     * 获取指定算法的公钥信息
     */
    @Override
    public PublicKeyInfo getPublicKeyInfo(EncryptionAlgorithm algorithm) {
        try {
            PublicKeyInfo publicKeyInfo = keyManagementService.getPublicKeyInfo(algorithm);

            log.debug("获取算法公钥信息成功 - 算法: {}, 版本: {}",
                    algorithm, publicKeyInfo.getKeyVersion());

            return publicKeyInfo;

        } catch (Exception e) {
            log.error("获取算法公钥信息失败 - 算法: {}", algorithm, e);
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_ERROR,
                    "获取算法公钥信息失败: " + algorithm, e);
        }
    }

    /**
     * 获取所有支持的算法及其公钥信息
     */
    @Override
    public Map<EncryptionAlgorithm, PublicKeyInfo> getSupportedAlgorithmsInfo() {
        Map<EncryptionAlgorithm, PublicKeyInfo> algorithms = new LinkedHashMap<>();
        Set<EncryptionAlgorithm> supportedAlgorithms = keyManagementService.getSupportedAlgorithms();

        for (EncryptionAlgorithm algorithm : ALGORITHM_PRIORITY) {
            if (supportedAlgorithms.contains(algorithm)) {
                try {
                    PublicKeyInfo info = getPublicKeyInfo(algorithm);
                    algorithms.put(algorithm, info);
                } catch (CryptoException e) {
                    log.warn("获取算法 {} 的公钥信息失败: {}", algorithm, e.getMessage());
                }
            }
        }

        log.debug("获取支持的算法信息 - 算法数量: {}", algorithms.size());
        return algorithms;
    }

    /**
     * 增强的健康检查（返回结构化数据）
     */
    @Override
    public BizResult<Map<String, Object>> enhancedHealthCheck(String testData) {
        long startTime = System.currentTimeMillis();

        try {
            Map<String, Object> result = new LinkedHashMap<>();

            // 1. 基本状态检查
            result.put("service", "PasswordTransmissionService");
            result.put("timestamp", LocalDateTime.now().toString());
            result.put("business", AUTH_BUSINESS);

            // 2. 密钥服务状态
            boolean keyServiceReady = keyManagementService.isReady();
            result.put("keyServiceReady", keyServiceReady);

            if (!keyServiceReady) {
                result.put("overallStatus", "UNHEALTHY");
                result.put("error", "密钥服务未就绪");
                return BizResult.success(result);
            }

            // 3. 配置验证
            boolean configValid = keyManagementService.validateConfiguration();
            result.put("configurationValid", configValid);

            // 4. 算法支持情况
            Set<EncryptionAlgorithm> supportedAlgorithms = keyManagementService.getSupportedAlgorithms();
            result.put("supportedAlgorithmsCount", supportedAlgorithms.size());
            result.put("supportedAlgorithms", supportedAlgorithms.stream()
                    .map(Enum::name)
                    .toList());

            // 5. 公钥可用性检查
            Map<String, Object> publicKeyStatus = new LinkedHashMap<>();
            for (EncryptionAlgorithm algorithm : supportedAlgorithms) {
                try {
                    PublicKeyInfo publicKeyInfo = getPublicKeyInfo(algorithm);
                    publicKeyStatus.put(algorithm.name(), Map.of(
                            "available", true,
                            "version", publicKeyInfo.getKeyVersion(),
                            "valid", publicKeyInfo.isValid()
                    ));
                } catch (Exception e) {
                    publicKeyStatus.put(algorithm.name(), Map.of(
                            "available", false,
                            "error", e.getMessage()
                    ));
                }
            }
            result.put("publicKeyStatus", publicKeyStatus);

            // 6. 功能测试（如果有测试数据）
            if (StringUtils.hasText(testData)) {
                Map<String, Object> functionTest = new LinkedHashMap<>();
                int successTests = 0;

                for (EncryptionAlgorithm algorithm : supportedAlgorithms) {
                    try {
                        String encrypted = encryptTestData(testData, algorithm);
                        String decrypted = decryptPasswordInternal(encrypted, algorithm);
                        boolean testPassed = testData.equals(decrypted);

                        functionTest.put(algorithm.name(), Map.of(
                                "testPassed", testPassed,
                                "encryptedLength", encrypted.length(),
                                "decryptedMatches", testPassed
                        ));

                        if (testPassed) {
                            successTests++;
                        }
                    } catch (Exception e) {
                        functionTest.put(algorithm.name(), Map.of(
                                "testPassed", false,
                                "error", e.getMessage()
                        ));
                    }
                }

                result.put("functionTest", functionTest);
                result.put("functionTestPassRate",
                        supportedAlgorithms.size() > 0 ?
                                (double) successTests / supportedAlgorithms.size() : 0);
            }

            // 7. 统计信息
            Map<String, Object> statistics = new LinkedHashMap<>();
            statistics.put("totalRequests", totalRequests.get());
            statistics.put("successRequests", successRequests.get());
            statistics.put("failedRequests", failedRequests.get());
            statistics.put("successRate", calculateSuccessRate());
            statistics.put("lastErrorTime", lastErrorTime.get() > 0 ?
                    LocalDateTime.ofEpochSecond(lastErrorTime.get() / 1000, 0, java.time.ZoneOffset.UTC) :
                    "Never");
            result.put("statistics", statistics);

            // 8. 性能信息
            result.put("responseTime", System.currentTimeMillis() - startTime);

            // 9. 总体状态评估
            boolean overallHealthy = keyServiceReady && configValid && !supportedAlgorithms.isEmpty();
            result.put("overallStatus", overallHealthy ? "HEALTHY" : "UNHEALTHY");

            log.info("增强健康检查完成 - 状态: {}, 耗时: {}ms",
                    result.get("overallStatus"), result.get("responseTime"));

            return BizResult.success(result);

        } catch (Exception e) {
            log.error("增强健康检查异常", e);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("service", "PasswordTransmissionService");
            errorResult.put("timestamp", LocalDateTime.now().toString());
            errorResult.put("overallStatus", "UNHEALTHY");
            errorResult.put("error", e.getMessage());

            return BizResult.success(errorResult);
        }
    }

    /**
     * 获取服务统计信息
     */
    @Override
    public Map<String, Object> getServiceStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("service", "PasswordTransmissionService");
        stats.put("business", AUTH_BUSINESS);
        stats.put("defaultAlgorithm", DEFAULT_ALGORITHM.name());
        stats.put("timestamp", LocalDateTime.now().toString());

        Map<String, Object> requestStats = new LinkedHashMap<>();
        requestStats.put("totalRequests", totalRequests.get());
        requestStats.put("successRequests", successRequests.get());
        requestStats.put("failedRequests", failedRequests.get());
        requestStats.put("successRate", calculateSuccessRate());
        requestStats.put("lastErrorTime", lastErrorTime.get());
        requestStats.put("algorithmCacheSize", algorithmCache.size());

        stats.put("requestStatistics", requestStats);

        // 添加密钥服务状态
        try {
            stats.put("keyServiceStatus", keyManagementService.getServiceStatus());
        } catch (Exception e) {
            stats.put("keyServiceStatus", "UNAVAILABLE: " + e.getMessage());
        }

        return stats;
    }

    /**
     * 批量解密密码
     */
    @Override
    public Map<String, String> decryptPasswordsBatch(Map<String, String> encryptedPasswords) {
        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, String> entry : encryptedPasswords.entrySet()) {
            try {
                String decrypted = decryptPassword(entry.getValue());
                result.put(entry.getKey(), decrypted);
            } catch (Exception e) {
                log.error("批量解密失败 - 键: {}", entry.getKey(), e);
                result.put(entry.getKey(), null); // 或者抛出异常，根据业务决定
            }
        }

        return result;
    }

    /**
     * 验证密码强度
     */
    @Override
    public boolean validatePasswordStrength(String password) {
        if (!StringUtils.hasText(password)) {
            return false;
        }

        // 基本强度规则
        if (password.length() < 8) {
            return false;
        }

        // 检查包含数字、字母、特殊字符等
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        // 至少满足3个条件
        int conditionsMet = 0;
        if (hasDigit) conditionsMet++;
        if (hasLower) conditionsMet++;
        if (hasUpper) conditionsMet++;
        if (hasSpecial) conditionsMet++;

        return conditionsMet >= 3;
    }

    // ============ 私有工具方法 ============

    /**
     * 内部解密方法
     */
    private String decryptPasswordInternal(String encryptedPassword, EncryptionAlgorithm algorithm) {
        try {
            // 使用 KeyManagementService 进行解密
            return keyManagementService.decryptAuto(algorithm, encryptedPassword);
        } catch (Exception e) {
            // 包装未知异常
            log.error("内部解密异常 - 算法: {}", algorithm, e);
            throw ExceptionFactory.crypto(ErrorCode.System.DECRYPT_FAILED,
                    "解密过程发生异常", e);
        }
    }

    /**
     * 测试数据加密
     */
    private String encryptTestData(String testData, EncryptionAlgorithm algorithm) {
        try {
            EncryptionService encryptionService = encryptionServiceFactory.getService(algorithm);
            PublicKeyInfo publicKeyInfo = getPublicKeyInfo(algorithm);

            if (publicKeyInfo != null) {
                String key = algorithm.name().startsWith("RSA") ?
                        publicKeyInfo.getPublicKey() : publicKeyInfo.getKey();
                return encryptionService.encrypt(testData, key);
            }

            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_ERROR,
                    "测试加密失败: 无法获取公钥信息");
        } catch (Exception e) {
            log.error("测试数据加密失败 - 算法: {}", algorithm, e);
            throw ExceptionFactory.crypto(ErrorCode.System.ENCRYPT_FAILED,
                    "测试加密失败", e);
        }
    }

    /**
     * 计算成功率
     */
    private String calculateSuccessRate() {
        long total = totalRequests.get();
        if (total == 0) {
            return "0%";
        }

        long success = successRequests.get();
        double rate = (double) success / total * 100;
        return String.format("%.2f%%", rate);
    }

    /**
     * 生成加密数据缓存键
     */
    private String getEncryptedDataKey(String encryptedData) {
        // 使用数据的前缀和后缀作为缓存键（避免存储完整数据）
        int length = encryptedData.length();
        if (length <= 20) {
            return encryptedData;
        }
        return encryptedData.substring(0, 10) + "..." + encryptedData.substring(length - 10);
    }

    /**
     * 重置统计信息（用于测试）
     */
    public void resetStatistics() {
        totalRequests.set(0);
        successRequests.set(0);
        failedRequests.set(0);
        lastErrorTime.set(0);
        algorithmCache.clear();
        log.info("密码传输服务统计信息已重置");
    }
}