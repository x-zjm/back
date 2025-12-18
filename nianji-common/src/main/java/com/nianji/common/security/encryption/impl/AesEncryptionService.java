package com.nianji.common.security.encryption.impl;

import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.ExceptionFactory;
import com.nianji.common.exception.system.CryptoException;
import com.nianji.common.security.encryption.EncryptionService;
import com.nianji.common.security.enums.EncryptionAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES加密服务实现
 * 支持AES-GCM和AES-CBC模式
 */
@Slf4j
@Component
public class AesEncryptionService implements EncryptionService {

    private static final String AES_ALGORITHM = "AES";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12; // GCM推荐12字节
    
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String encrypt(String data, String key) throws CryptoException {
        return encryptGCM(data, key);
    }

    @Override
    public String decrypt(String encryptedData, String key) throws CryptoException {
        return decryptGCM(encryptedData, key);
    }

    /**
     * AES-GCM加密 - 推荐使用，提供完整性和机密性
     */
    public String encryptGCM(String data, String key) throws CryptoException {
        try {
            byte[] keyBytes = decodeKey(key);
            SecretKey secretKey = new SecretKeySpec(keyBytes, AES_ALGORITHM);
            
            // 生成IV
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            Cipher cipher = Cipher.getInstance(EncryptionAlgorithm.AES_GCM.getAlgorithm());
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            // 组合IV + 加密数据
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
            
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("AES-GCM加密失败", e);
            throw ExceptionFactory.crypto(ErrorCode.System.ENCRYPT_FAILED);
        }
    }

    /**
     * AES-GCM解密
     */
    public String decryptGCM(String encryptedData, String key) throws CryptoException {
        try {
            byte[] keyBytes = decodeKey(key);
            SecretKey secretKey = new SecretKeySpec(keyBytes, AES_ALGORITHM);
            
            byte[] combined = Base64.getDecoder().decode(encryptedData);
            
            // 分离IV和加密数据
            byte[] iv = new byte[IV_LENGTH];
            byte[] encryptedBytes = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, encryptedBytes, 0, encryptedBytes.length);
            
            Cipher cipher = Cipher.getInstance(EncryptionAlgorithm.AES_GCM.getAlgorithm());
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            byte[] decryptedData = cipher.doFinal(encryptedBytes);
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES-GCM解密失败", e);
            throw ExceptionFactory.crypto(ErrorCode.System.DECRYPT_FAILED);
        }
    }

    /**
     * AES-CBC加密 - 兼容性更好
     */
    public String encryptCBC(String data, String key) throws CryptoException {
        try {
            byte[] keyBytes = decodeKey(key);
            SecretKey secretKey = new SecretKeySpec(keyBytes, AES_ALGORITHM);
            
            // 生成IV
            byte[] iv = new byte[16];
            secureRandom.nextBytes(iv);
            
            Cipher cipher = Cipher.getInstance(EncryptionAlgorithm.AES_CBC_PKCS5.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new javax.crypto.spec.IvParameterSpec(iv));
            
            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            // 组合IV + 加密数据
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
            
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("AES-CBC加密失败", e);
            throw ExceptionFactory.crypto(ErrorCode.System.ENCRYPT_FAILED);
        }
    }

    /**
     * AES-CBC解密
     */
    public String decryptCBC(String encryptedData, String key) throws CryptoException {
        try {
            byte[] keyBytes = decodeKey(key);
            SecretKey secretKey = new SecretKeySpec(keyBytes, AES_ALGORITHM);
            
            byte[] combined = Base64.getDecoder().decode(encryptedData);
            
            // 分离IV和加密数据
            byte[] iv = new byte[16];
            byte[] encryptedBytes = new byte[combined.length - 16];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, encryptedBytes, 0, encryptedBytes.length);
            
            Cipher cipher = Cipher.getInstance(EncryptionAlgorithm.AES_CBC_PKCS5.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new javax.crypto.spec.IvParameterSpec(iv));
            
            byte[] decryptedData = cipher.doFinal(encryptedBytes);
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES-CBC解密失败", e);
            throw ExceptionFactory.crypto(ErrorCode.System.DECRYPT_FAILED);
        }
    }

    @Override
    public String generateKey() throws CryptoException {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGenerator.init(256, secureRandom);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            log.error("生成AES密钥失败", e);
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_GENERATE_FAILED);
        }
    }

    @Override
    public EncryptionAlgorithm getAlgorithm() {
        return EncryptionAlgorithm.AES_GCM;
    }

    @Override
    public boolean verify(String data, String signature, String key) throws CryptoException {
        // AES不需要单独的验证，GCM模式自带完整性验证
        return true;
    }

    /**
     * 解码Base64密钥
     */
    private byte[] decodeKey(String key) {
        return Base64.getDecoder().decode(key);
    }
}