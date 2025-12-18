package com.nianji.common.security.enums;

import lombok.Getter;

/**
 * 加密算法枚举
 */
@Getter
public enum EncryptionAlgorithm {
    // 对称加密
    // 只做兼容使用
    AES_CBC_PKCS5("AES/CBC/PKCS5Padding", 256),
    // 如果不知道，请使用这个
    AES_GCM("AES/GCM/NoPadding", 256),

    // 非对称加密
    // 只做兼容使用
    RSA_ECB_PKCS1("RSA/ECB/PKCS1Padding", 2048),
    // 如果不知道，请使用这个
    RSA_ECB_OAEP("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", 2048),
    
    // 哈希算法
    SHA256("SHA-256", 0),
    SHA512("SHA-512", 0),
    MD5("MD5", 0),
    
    // 密码哈希
    BCRYPT("BCrypt", 0),
    PBKDF2("PBKDF2WithHmacSHA256", 0);

    private final String algorithm;
    private final int keySize;

    EncryptionAlgorithm(String algorithm, int keySize) {
        this.algorithm = algorithm;
        this.keySize = keySize;
    }

}