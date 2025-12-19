package com.nianji.auth.service;

import com.nianji.auth.service.impl.PasswordTransmissionServiceImpl;
import com.nianji.common.security.model.PublicKeyInfo;
import com.nianji.common.security.enums.EncryptionAlgorithm;
import com.nianji.common.reqres.BizResult;

import java.util.Map;

/**
 * 密码传输服务接口
 * <p>
 * 该接口负责处理密码的安全传输，包括加密、解密、密钥管理等功能。 采用非对称加密算法保证密码在传输过程中的安全性。
 *
 * @author zhangjinming
 * @version 0.0.1
 * @see PasswordTransmissionServiceImpl
 */
public interface PasswordTransmissionService {

    String decryptPassword(String encryptedPassword, EncryptionAlgorithm algorithm);

    /**
     * 解密密码
     * <p>
     * 该方法用于解密前端使用公钥加密的密码。 解密过程使用对应的私钥完成，确保只有服务端能够解密密码。
     *
     * @param encryptedPassword
     *         前端加密后的密码
     * @return 解密后的明文密码
     * @throws com.nianji.common.exception.system.CryptoException
     *         当解密失败时抛出
     */
    String decryptPassword(String encryptedPassword);

    /**
     * 验证加密数据格式
     * <p>
     * 该方法用于验证前端传来的加密数据格式是否正确， 是否包含必要的元数据（如算法标识、密钥版本等）。
     *
     * @param encryptedPassword
     *         待验证的加密密码
     * @return true - 格式正确，false - 格式错误
     */
    boolean validateEncryptedData(String encryptedPassword);

    String getCurrentEncryptionInfo();

    /**
     * 健康检查
     * <p>
     * 该方法用于检查密码传输服务的整体健康状况， 包括密钥服务状态、各算法支持情况、基本功能测试等。
     *
     * @param data
     *         可选的测试数据，如果提供则会执行完整的加密-解密测试
     * @return 健康检查结果字符串，包含各项检查的详细状态
     * @example <pre>{@code
     * // 基础健康检查
     * String healthStatus = passwordTransmissionService.healthCheck(null);
     *
     * // 完整功能测试
     * String healthStatus = passwordTransmissionService.healthCheck("test-data-123");
     * }</pre>
     */
    String healthCheck(String data);

    /**
     * 获取当前用于前端加密的公钥信息
     */
    PublicKeyInfo getPublicKeyInfo();

    /**
     * 获取指定算法的公钥信息
     * <p>
     * 该方法返回指定加密算法的公钥信息，用于特定算法场景下的前端加密。
     *
     * @param algorithm
     *         指定的加密算法
     * @return 指定算法的公钥信息
     * @throws com.nianji.common.exception.system.CryptoException
     *         当算法不支持或获取失败时抛出
     */
    PublicKeyInfo getPublicKeyInfo(EncryptionAlgorithm algorithm);

    /**
     * 获取所有支持的算法及其公钥信息
     * <p>
     * 该方法返回当前支持的所有加密算法及其对应的公钥信息， 用于前端根据环境支持情况选择合适的加密算法。
     *
     * @return 算法到公钥信息的映射表，按算法优先级排序
     */
    Map<EncryptionAlgorithm, PublicKeyInfo> getSupportedAlgorithmsInfo();

    /**
     * 增强的健康检查（返回结构化数据）
     * <p>
     * 该方法提供更详细的健康检查信息，以结构化的方式返回各项检查结果， 适用于系统监控和自动化健康检查场景。
     *
     * @param testData
     *         可选的测试数据
     * @return 结构化的健康检查结果
     */
    BizResult<Map<String, Object>> enhancedHealthCheck(String testData);

    /**
     * 获取服务统计信息
     * <p>
     * 该方法返回密码传输服务的运行统计信息，包括请求量、成功率、错误统计等， 用于监控系统运行状态和性能分析。
     *
     * @return 服务统计信息
     */
    Map<String, Object> getServiceStatistics();

    /**
     * 批量解密密码
     * <p>
     * 该方法支持批量解密多个加密密码，提高处理效率。 适用于用户批量导入等需要处理多个加密密码的场景。
     *
     * @param encryptedPasswords
     *         加密密码映射表，key为标识符，value为加密密码
     * @return 解密后的密码映射表，key与输入相同，value为解密后的明文密码
     */
    Map<String, String> decryptPasswordsBatch(Map<String, String> encryptedPasswords);

    /**
     * 验证密码强度
     * <p>
     * 该方法对解密后的密码进行强度验证，检查是否符合安全策略要求。
     *
     * @param password
     *         待验证的密码
     * @return true - 密码强度符合要求，false - 密码强度不足
     */
    boolean validatePasswordStrength(String password);

    /**
     * 测试用加密方法
     * <p>
     * 该方法仅供测试使用，用于在本地环境中模拟前端加密过程。
     *
     * @param plaintext 明文
     * @param algorithm 加密算法
     * @return 加密后的密文
     */
    String encryptForTest(String plaintext, EncryptionAlgorithm algorithm);
}