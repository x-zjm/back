package com.nianji.common.security.encryption.impl;

import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.ExceptionFactory;
import com.nianji.common.exception.system.CryptoException;
import com.nianji.common.security.encryption.EncryptionService;
import com.nianji.common.security.enums.EncryptionAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * 哈希服务实现
 * 支持MD5、SHA256、SHA512、BCrypt、PBKDF2等算法
 */
@Slf4j
@Component
public class HashService implements EncryptionService {

    private static final SecureRandom secureRandom = new SecureRandom();
    private final PasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();

    @Override
    public String encrypt(String data, String salt) throws CryptoException {
        // 对于哈希，encrypt就是生成哈希值
        return hashSHA256(data, salt);
    }

    @Override
    public String decrypt(String encryptedData, String key) throws CryptoException {
        throw ExceptionFactory.crypto(ErrorCode.System.UNSUPPORTED_DECRYPT);
    }

    @Override
    public String generateKey() throws CryptoException {
        // 生成随机盐
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    @Override
    public EncryptionAlgorithm getAlgorithm() {
        return EncryptionAlgorithm.SHA256;
    }

    @Override
    public boolean verify(String data, String hash, String salt) throws CryptoException {
        String computedHash = hashSHA256(data, salt);
        return MessageDigest.isEqual(
            computedHash.getBytes(StandardCharsets.UTF_8),
            hash.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * SHA256哈希
     */
    public String hashSHA256(String data, String salt) throws CryptoException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            if (salt != null) {
                digest.update(salt.getBytes(StandardCharsets.UTF_8));
            }
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA256算法不支持", e);
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_UNKNOWN);
        }
    }

    /**
     * SHA512哈希
     */
    public String hashSHA512(String data, String salt) throws CryptoException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            if (salt != null) {
                digest.update(salt.getBytes(StandardCharsets.UTF_8));
            }
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA512算法不支持", e);
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_UNKNOWN);
        }
    }

    /**
     * MD5哈希（不推荐用于安全场景）
     */
    public String hashMD5(String data) throws CryptoException {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            
            // 转换为16进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5算法不支持", e);
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_UNKNOWN);
        }
    }

    /**
     * BCrypt哈希 - 推荐用于密码存储
     */
    public String hashBCrypt(String password) {
        return bcryptEncoder.encode(password);
    }

    /**
     * 验证BCrypt哈希
     */
    public boolean verifyBCrypt(String password, String hashedPassword) {
        return bcryptEncoder.matches(password, hashedPassword);
    }

    /**
     * PBKDF2哈希 - 用于需要可配置迭代次数的场景
     */
    public String hashPBKDF2(String password, String salt, int iterations) throws CryptoException {
        try {
            KeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt.getBytes(StandardCharsets.UTF_8),
                iterations,
                256
            );
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("PBKDF2哈希失败", e);
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_UNKNOWN);
        }
    }

    /**
     * 生成安全的随机字符串
     */
    public String generateSecureRandom(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}