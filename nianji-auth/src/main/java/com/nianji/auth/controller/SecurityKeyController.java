package com.nianji.auth.controller;

import com.nianji.common.reqres.BizResult;
import com.nianji.common.security.enums.EncryptionAlgorithm;
import com.nianji.common.security.model.PublicKeyInfo;
import com.nianji.auth.service.PasswordTransmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 密码传输控制器
 * <p>
 * 提供密码加密传输相关的REST接口，确保密码在传输过程中的安全性。 支持公钥获取、密码解密、健康检查等功能。
 *
 * @author zhangjinming
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/auth/security")
@RequiredArgsConstructor
public class SecurityKeyController {

    private final PasswordTransmissionService passwordTransmissionService;

    /**
     * 获取当前公钥信息
     * <p>
     * 该接口返回当前用于密码加密的公钥信息，前端使用此公钥对密码进行加密后传输。 无需认证即可访问，供所有需要密码加密的前端页面使用。
     *
     * @return 包含公钥信息的成功结果
     * @apiNote 前端使用示例：
     * <pre>{@code
     * // 1. 调用此接口获取公钥信息
     * // 2. 使用公钥对密码进行加密
     * // 3. 将加密后的密码传输到后端
     * }</pre>
     */
    @GetMapping("/public-key")
    public BizResult<PublicKeyInfo> getPublicKey() {
        try {
            PublicKeyInfo publicKeyInfo = passwordTransmissionService.getPublicKeyInfo();

            log.debug("提供认证业务公钥信息 - 算法: {}, 版本: {}",
                    publicKeyInfo.getAlgorithm(),
                    publicKeyInfo.getKeyVersion());

            return BizResult.success(publicKeyInfo);

        } catch (Exception e) {
            log.error("获取公钥信息失败", e);
            return BizResult.fail("GET_PUBLIC_KEY_FAILED", "获取公钥信息失败");
        }
    }

    /**
     * 获取指定算法的公钥信息
     * <p>
     * 该接口返回指定加密算法的公钥信息，用于特定算法需求的场景。
     *
     * @param algorithm
     *         加密算法，如 RSA_ECB_OAEP、AES_GCM 等
     * @return 包含指定算法公钥信息的成功结果
     */
    @GetMapping("/public-key/{algorithm}")
    public BizResult<PublicKeyInfo> getPublicKeyByAlgorithm(
            @PathVariable EncryptionAlgorithm algorithm) {
        try {
            PublicKeyInfo publicKeyInfo = passwordTransmissionService.getPublicKeyInfo(algorithm);
            return BizResult.success(publicKeyInfo);
        } catch (Exception e) {
            log.error("获取算法公钥信息失败 - 算法: {}", algorithm, e);
            return BizResult.fail("GET_ALGORITHM_PUBLIC_KEY_FAILED", "获取算法公钥信息失败: " + algorithm);
        }
    }

    /**
     * 解密密码（自动算法探测）
     * <p>
     * 该接口对前端传输的加密密码进行解密，自动探测使用的加密算法。 需要具有密码解密权限才能访问。
     *
     * @param request
     *         包含加密密码的请求体
     * @return 包含解密状态的成功结果
     * @apiNote 请求体示例：
     * <pre>{@code
     * {
     *   "encryptedPassword": "MIIBPAYJKoZIhvcNAQcDoIIBL..."
     * }
     * }</pre>
     */
    @PostMapping("/decrypt-password")
    @PreAuthorize("hasAuthority('auth:password:decrypt')")
    public BizResult<Map<String, String>> decryptPassword(
            @RequestBody Map<String, String> request) {
        try {
            String encryptedPassword = request.get("encryptedPassword");

            if (encryptedPassword == null || encryptedPassword.trim().isEmpty()) {
                return BizResult.fail("INVALID_REQUEST", "加密密码不能为空");
            }

            // 验证加密数据格式
            if (!passwordTransmissionService.validateEncryptedData(encryptedPassword)) {
                return BizResult.fail("INVALID_ENCRYPTED_DATA", "加密数据格式无效");
            }

            // 执行解密
            String decryptedPassword = passwordTransmissionService.decryptPassword(encryptedPassword);

            Map<String, String> result = Map.of(
                    "status", "SUCCESS",
                    "message", "密码解密成功",
                    "decryptedLength", String.valueOf(decryptedPassword.length())
            );

            log.debug("密码解密成功 - 数据长度: {}", encryptedPassword.length());
            return BizResult.success(result);

        } catch (Exception e) {
            log.error("密码解密失败", e);
            return BizResult.fail("DECRYPT_PASSWORD_FAILED", "密码解密失败: " + e.getMessage());
        }
    }

    /**
     * 使用指定算法解密密码
     * <p>
     * 该接口使用指定的加密算法对密码进行解密，适用于已知前端使用特定算法的场景。
     *
     * @param algorithm
     *         加密算法
     * @param request
     *         包含加密密码的请求体
     * @return 包含解密状态的成功结果
     */
    @PostMapping("/decrypt-password/{algorithm}")
    @PreAuthorize("hasAuthority('auth:password:decrypt')")
    public BizResult<Map<String, String>> decryptPasswordWithAlgorithm(
            @PathVariable EncryptionAlgorithm algorithm,
            @RequestBody Map<String, String> request) {
        try {
            String encryptedPassword = request.get("encryptedPassword");

            if (encryptedPassword == null || encryptedPassword.trim().isEmpty()) {
                return BizResult.fail("INVALID_REQUEST", "加密密码不能为空");
            }

            // 执行解密
            String decryptedPassword = passwordTransmissionService.decryptPassword(encryptedPassword, algorithm);

            Map<String, String> result = Map.of(
                    "status", "SUCCESS",
                    "message", "密码解密成功",
                    "algorithm", algorithm.name(),
                    "decryptedLength", String.valueOf(decryptedPassword.length())
            );

            log.debug("密码解密成功 - 算法: {}, 数据长度: {}", algorithm, encryptedPassword.length());
            return BizResult.success(result);

        } catch (Exception e) {
            log.error("密码解密失败 - 算法: {}", algorithm, e);
            return BizResult.fail("DECRYPT_PASSWORD_FAILED", "密码解密失败: " + e.getMessage());
        }
    }

    /**
     * 基础健康检查接口
     * <p>
     * 该接口提供加密服务的基础健康检查，返回简明的健康状态信息。 可用于负载均衡健康检查、服务监控等场景。
     *
     * @param data
     *         可选的测试数据，如果提供会执行完整的加密-解密测试
     * @return 包含健康状态的成功结果
     */
    @GetMapping("/health")
    public BizResult<String> healthCheck(
            @RequestParam(required = false) String data) {
        try {
            String healthStatus = passwordTransmissionService.healthCheck(data);
            return BizResult.success(healthStatus);
        } catch (Exception e) {
            log.error("健康检查异常", e);
            return BizResult.fail("HEALTH_CHECK_FAILED", "健康检查异常: " + e.getMessage());
        }
    }

    /**
     * 增强健康检查接口
     * <p>
     * 该接口提供详细的健康检查信息，以结构化的方式返回各项检查结果。 适用于系统监控、运维管理等需要详细状态信息的场景。
     *
     * @param testData
     *         可选的测试数据
     * @return 包含详细健康检查结果的成功结果
     */
    @GetMapping("/health/detailed")
    @PreAuthorize("hasAuthority('auth:security:read')")
    public BizResult<Map<String, Object>> enhancedHealthCheck(
            @RequestParam(required = false) String testData) {
        try {
            return passwordTransmissionService.enhancedHealthCheck(testData);
        } catch (Exception e) {
            log.error("增强健康检查异常", e);
            return BizResult.fail("ENHANCED_HEALTH_CHECK_FAILED", "增强健康检查异常: " + e.getMessage());
        }
    }

    /**
     * 获取服务状态信息
     * <p>
     * 该接口返回密码传输服务的详细状态信息，包括配置、统计、监控数据等。 需要具有安全读取权限才能访问。
     *
     * @return 包含服务状态信息的成功结果
     */
    @GetMapping("/status")
    @PreAuthorize("hasAuthority('auth:security:read')")
    public BizResult<Map<String, Object>> getServiceStatus() {
        try {
            Map<String, Object> status = passwordTransmissionService.getServiceStatistics();
            return BizResult.success(status);
        } catch (Exception e) {
            log.error("获取服务状态失败", e);
            return BizResult.fail("GET_STATUS_FAILED", "获取服务状态失败");
        }
    }

    /**
     * 获取支持的算法列表
     * <p>
     * 该接口返回当前支持的所有加密算法及其描述信息。 前端可根据此信息选择合适的加密算法。
     *
     * @return 包含支持算法列表的成功结果
     */
    @GetMapping("/supported-algorithms")
    public BizResult<Map<EncryptionAlgorithm, PublicKeyInfo>> getSupportedAlgorithms() {
        try {
            Map<EncryptionAlgorithm, PublicKeyInfo> algorithms =
                    passwordTransmissionService.getSupportedAlgorithmsInfo();
            return BizResult.success(algorithms);
        } catch (Exception e) {
            log.error("获取支持算法列表失败", e);
            return BizResult.fail("GET_ALGORITHMS_FAILED", "获取算法列表失败");
        }
    }

    /**
     * 批量解密密码接口
     * <p>
     * 该接口支持批量解密多个加密密码，适用于用户批量导入等场景。 需要具有批量解密权限才能访问。
     *
     * @param encryptedPasswords
     *         加密密码映射表
     * @return 包含批量解密结果的成功结果
     * @apiNote 请求体示例：
     * <pre>{@code
     * {
     *   "user1": "MIIBPAYJKoZIhvcNAQcDoIIBL...",
     *   "user2": "MIIBPAYJKoZIhvcNAQcDoIIBL...",
     *   "user3": "MIIBPAYJKoZIhvcNAQcDoIIBL..."
     * }
     * }</pre>
     */
    @PostMapping("/decrypt-passwords/batch")
    @PreAuthorize("hasAuthority('auth:password:decrypt:batch')")
    public BizResult<Map<String, String>> decryptPasswordsBatch(
            @RequestBody Map<String, String> encryptedPasswords) {
        try {
            if (encryptedPasswords == null || encryptedPasswords.isEmpty()) {
                return BizResult.fail("INVALID_REQUEST", "密码列表不能为空");
            }

            if (encryptedPasswords.size() > 100) {
                return BizResult.fail("BATCH_SIZE_EXCEEDED", "批量解密数量超出限制");
            }

            // 执行批量解密
            Map<String, String> decryptedPasswords =
                    passwordTransmissionService.decryptPasswordsBatch(encryptedPasswords);

            Map<String, String> result = Map.of(
                    "status", "SUCCESS",
                    "message", "批量解密完成",
                    "totalCount", String.valueOf(encryptedPasswords.size()),
                    "successCount", String.valueOf(decryptedPasswords.values().stream()
                            .filter(v -> v != null).count())
            );

            log.info("批量解密完成 - 总数: {}, 成功: {}",
                    encryptedPasswords.size(),
                    decryptedPasswords.values().stream().filter(v -> v != null).count());

            return BizResult.success(result);

        } catch (Exception e) {
            log.error("批量解密失败", e);
            return BizResult.fail("BATCH_DECRYPT_FAILED", "批量解密失败: " + e.getMessage());
        }
    }

    /**
     * 验证密码强度
     * <p>
     * 该接口对密码进行强度验证，检查是否符合安全策略要求。 可用于用户注册、密码修改等场景的密码强度校验。
     *
     * @param request
     *         包含待验证密码的请求体
     * @return 包含密码强度验证结果的成功结果
     */
    @PostMapping("/validate-password-strength")
    public BizResult<Map<String, Object>> validatePasswordStrength(
            @RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");

            if (password == null) {
                return BizResult.fail("INVALID_REQUEST", "密码不能为空");
            }

            boolean isValid = passwordTransmissionService.validatePasswordStrength(password);

            Map<String, Object> result = Map.of(
                    "valid", isValid,
                    "message", isValid ? "密码强度符合要求" : "密码强度不足",
                    "suggestions", getPasswordSuggestions(password)
            );

            return BizResult.success(result);

        } catch (Exception e) {
            log.error("密码强度验证失败", e);
            return BizResult.fail("VALIDATE_PASSWORD_STRENGTH_FAILED", "密码强度验证失败");
        }
    }

    /**
     * 获取当前加密信息
     * <p>
     * 该接口返回当前加密服务的配置信息，用于日志记录和系统监控。
     *
     * @return 包含加密信息的成功结果
     */
    @GetMapping("/encryption-info")
    @PreAuthorize("hasAuthority('auth:security:read')")
    public BizResult<String> getEncryptionInfo() {
        try {
            String encryptionInfo = passwordTransmissionService.getCurrentEncryptionInfo();
            return BizResult.success(encryptionInfo);
        } catch (Exception e) {
            log.error("获取加密信息失败", e);
            return BizResult.fail("GET_ENCRYPTION_INFO_FAILED", "获取加密信息失败");
        }
    }

    // ============ 私有工具方法 ============

    /**
     * 获取密码改进建议
     */
    private Map<String, String> getPasswordSuggestions(String password) {
        Map<String, String> suggestions = new java.util.HashMap<>();

        if (password.length() < 8) {
            suggestions.put("length", "密码长度至少8位");
        }

        if (!password.matches(".*\\d.*")) {
            suggestions.put("digit", "建议包含数字");
        }

        if (!password.matches(".*[a-z].*")) {
            suggestions.put("lowercase", "建议包含小写字母");
        }

        if (!password.matches(".*[A-Z].*")) {
            suggestions.put("uppercase", "建议包含大写字母");
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            suggestions.put("special", "建议包含特殊字符");
        }

        return suggestions;
    }
}