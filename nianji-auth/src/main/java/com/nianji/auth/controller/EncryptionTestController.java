package com.nianji.auth.controller;

import com.nianji.auth.service.PasswordTransmissionService;
import com.nianji.common.security.enums.EncryptionAlgorithm;
import com.nianji.common.security.model.PublicKeyInfo;
import com.nianji.common.reqres.BizResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 加解密测试接口
 * <p>
 * 该控制器仅在开发环境(dev)、测试环境(test)和本地环境(local)中启用，
 * 用于方便地测试加解密功能，获取明文对应的密文。
 */
@Slf4j
@RestController
@Profile({"dev", "test", "local"})
@RequestMapping("/auth/encryption/test")
@RequiredArgsConstructor
public class EncryptionTestController {

    private final PasswordTransmissionService passwordTransmissionService;

    /**
     * 加密测试接口
     * <p>
     * 该接口接收明文密码，返回使用系统支持的各种算法加密后的密文。
     *
     * @param plaintext 明文密码
     * @return 包含各种算法加密结果的映射表
     */
    @PostMapping("/encrypt")
    public BizResult<Map<String, Object>> encryptTest(@RequestBody String plaintext) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            result.put("plaintext", plaintext);
            result.put("algorithms", new HashMap<>());
            
            // 对每种算法进行加密测试
            for (EncryptionAlgorithm algorithm : EncryptionAlgorithm.values()) {
                try {
                    // 获取公钥信息
                    PublicKeyInfo publicKeyInfo = passwordTransmissionService.getPublicKeyInfo(algorithm);
                    
                    // 使用实际的加密服务进行加密
                    String encrypted = passwordTransmissionService.encryptForTest(plaintext, algorithm);
                    
                    ((Map<String, Object>) result.get("algorithms")).put(algorithm.name(), Map.of(
                            "encrypted", encrypted,
                            "publicKeyInfo", publicKeyInfo,
                            "status", "success"
                    ));
                } catch (Exception e) {
                    ((Map<String, Object>) result.get("algorithms")).put(algorithm.name(), Map.of(
                            "error", e.getMessage(),
                            "status", "failed"
                    ));
                    log.warn("加密测试失败 - 算法: {}, 错误: {}", algorithm, e.getMessage());
                }
            }
            
            return BizResult.success(result);
        } catch (Exception e) {
            log.error("加密测试异常", e);
            return BizResult.fail("ENCRYPT_TEST_FAILED", "加密测试异常: " + e.getMessage());
        }
    }

    /**
     * 解密测试接口
     * <p>
     * 该接口接收密文，返回解密后的明文。
     *
     * @param ciphertext 密文
     * @return 解密结果
     */
    @PostMapping("/decrypt")
    public BizResult<Map<String, Object>> decryptTest(@RequestParam String ciphertext) {
        try {
            String decrypted = passwordTransmissionService.decryptPassword(ciphertext);
            
            Map<String, Object> result = Map.of(
                    "ciphertext", ciphertext,
                    "decrypted", decrypted
            );
            
            return BizResult.success(result);
        } catch (Exception e) {
            log.error("解密测试异常", e);
            return BizResult.fail("DECRYPT_TEST_FAILED", "解密测试异常: " + e.getMessage());
        }
    }

    /**
     * 加解密全流程测试接口
     *
     * @param plaintext 明文密码
     * @param algorithm 算法名称
     * @return 测试结果
     */
    @PostMapping("/full-test")
    public BizResult<Map<String, Object>> fullTest(
            @RequestParam String plaintext,
            @RequestParam String algorithm) {
        try {
            EncryptionAlgorithm encAlgorithm = EncryptionAlgorithm.valueOf(algorithm.toUpperCase());
            
            // 加密
            String encrypted = passwordTransmissionService.encryptForTest(plaintext, encAlgorithm);
            
            // 解密
            String decrypted = passwordTransmissionService.decryptPassword(encrypted);
            
            // 验证
            boolean match = plaintext.equals(decrypted);
            
            Map<String, Object> result = new HashMap<>();
            result.put("plaintext", plaintext);
            result.put("algorithm", algorithm);
            result.put("encrypted", encrypted);
            result.put("decrypted", decrypted);
            result.put("match", match);
            result.put("status", match ? "success" : "failed");
            
            return BizResult.success(result);
        } catch (Exception e) {
            log.error("全流程测试异常", e);
            return BizResult.fail("FULL_TEST_FAILED", "全流程测试异常: " + e.getMessage());
        }
    }
}