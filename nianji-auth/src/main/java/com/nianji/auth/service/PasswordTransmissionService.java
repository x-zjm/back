package com.nianji.auth.service;

import com.nianji.common.reqres.BizResult;
import com.nianji.common.security.enums.EncryptionAlgorithm;
import com.nianji.common.security.model.PublicKeyInfo;

import java.util.Map;

/**
 * 密码传输服务接口
 * <p>
 * 该服务负责处理前端密码的加密传输和后端解密验证，确保密码在传输过程中的安全性。 支持多种加密算法和自动算法探测，提供完整的密钥管理和健康检查功能。
 *
 * @author zhangjinming
 * @version 1.0.0
 */
public interface PasswordTransmissionService {

    /**
     * 使用指定算法解密前端传输的加密密码
     * <p>
     * 该方法使用指定的加密算法对前端传输的加密密码进行解密，返回明文密码。 适用于已知前端使用特定加密算法的场景。
     *
     * @param encryptedPassword
     *         前端加密的密码数据（Base64编码）
     * @param algorithm
     *         加密算法枚举，如 RSA_ECB_OAEP、AES_GCM 等
     * @return 解密后的明文密码
     * @throws com.nianji.common.exception.system.CryptoException
     *         当解密失败时抛出
     * @example <pre>{@code
     * String password = passwordTransmissionService.decryptPassword(
     *     "MIIBPAYJKoZIhvcNAQcDoIIBL...",
     *     EncryptionAlgorithm.RSA_ECB_OAEP
     * );
     * }</pre>
     */
    String decryptPassword(String encryptedPassword, EncryptionAlgorithm algorithm);

    /**
     * 自动探测算法并解密前端传输的加密密码
     * <p>
     * 该方法会自动尝试所有支持的加密算法，按优先级顺序进行解密尝试， 直到找到能够成功解密的算法。适用于前端加密算法不确定的场景。
     *
     * @param encryptedPassword
     *         前端加密的密码数据（Base64编码）
     * @return 解密后的明文密码
     * @throws com.nianji.common.exception.system.CryptoException
     *         当所有算法解密均失败时抛出
     * @example <pre>{@code
     * String password = passwordTransmissionService.decryptPassword(
     *     "MIIBPAYJKoZIhvcNAQcDoIIBL..."
     * );
     * }</pre>
     */
    String decryptPassword(String encryptedPassword);

    /**
     * 获取当前用于前端加密的公钥信息
     * <p>
     * 该方法返回当前激活的公钥信息，供前端使用对应的公钥对密码进行加密。 返回的信息包含算法类型、密钥版本、公钥内容等。
     *
     * @return 公钥信息对象，包含算法、版本、公钥等详细信息
     * @throws com.nianji.common.exception.system.CryptoException
     *         当获取公钥失败时抛出
     * @example <pre>{@code
     * PublicKeyInfo publicKeyInfo = passwordTransmissionService.getPublicKeyInfo();
     * // 前端使用 publicKeyInfo.getPublicKey() 进行加密
     * }</pre>
     */
    PublicKeyInfo getPublicKeyInfo();

    /**
     * 验证加密数据的格式是否正确
     * <p>
     * 该方法对前端传输的加密数据进行格式验证，确保数据是有效的Base64编码， 并且长度在合理范围内。用于在解密前进行数据校验。
     *
     * @param encryptedData
     *         待验证的加密数据
     * @return true - 数据格式正确，false - 数据格式错误
     * @throws com.nianji.common.exception.system.CryptoException
     *         当数据为空或格式明显错误时抛出
     * @example <pre>{@code
     * boolean isValid = passwordTransmissionService.validateEncryptedData(encryptedPassword);
     * if (isValid) {
     *     String password = decryptPassword(encryptedPassword);
     * }
     * }</pre>
     */
    boolean validateEncryptedData(String encryptedData);

    /**
     * 获取当前加密配置信息（用于日志记录和调试）
     * <p>
     * 该方法返回当前加密服务的配置状态信息，包括使用的算法、密钥版本等， 主要用于日志记录、系统监控和故障排查。
     *
     * @return 加密配置信息字符串，格式为"算法: xxx, 版本: xxx, 状态: xxx"
     * @example <pre>{@code
     * String encryptionInfo = passwordTransmissionService.getCurrentEncryptionInfo();
     * log.info("当前加密配置: {}", encryptionInfo);
     * // 输出: "当前加密配置: 算法: RSA_ECB_OAEP, 版本: v1a2b3c4d, 状态: 有效"
     * }</pre>
     */
    String getCurrentEncryptionInfo();

    /**
     * 加密服务健康检查
     * <p>
     * 该方法对加密服务的各项功能进行健康检查，包括密钥服务状态、加解密功能等。 可以传入测试数据进行完整的加密-解密流程验证。
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
}