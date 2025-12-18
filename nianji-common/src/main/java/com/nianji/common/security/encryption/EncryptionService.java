package com.nianji.common.security.encryption;

import com.nianji.common.exception.business.BusinessException;
import com.nianji.common.security.enums.EncryptionAlgorithm;

/**
 * 通用加密服务接口
 */
public interface EncryptionService {
    
    /**
     * 加密数据
     */
    String encrypt(String data, String key) throws BusinessException;
    
    /**
     * 解密数据
     */
    String decrypt(String encryptedData, String key) throws BusinessException;
    
    /**
     * 生成密钥
     */
    String generateKey() throws BusinessException;
    
    /**
     * 支持的算法
     */
    EncryptionAlgorithm getAlgorithm();
    
    /**
     * 验证数据完整性
     */
    boolean verify(String data, String signature, String key) throws BusinessException;
}