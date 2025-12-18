package com.nianji.common.constant;

/**
 * 安全相关常量
 */
public class SecurityConstants {

    // 缓存键常量
    public static final String CACHE_PUBLIC_KEY_PREFIX = "nianji:security:public_key:";
    public static final String CACHE_ENCRYPTION_KEY_PREFIX = "nianji:security:encryption_key:";

    // 默认业务标识
    public static final String DEFAULT_BUSINESS_AUTH = "auth";
    public static final String DEFAULT_BUSINESS_USER = "user";
    public static final String DEFAULT_BUSINESS_PAYMENT = "payment";

    // 密钥版本
    public static final String DEFAULT_KEY_VERSION = "v1";

    // 算法常量
    public static final String ALGORITHM_RSA = "RSA";
    public static final String ALGORITHM_AES = "AES";
    public static final String ALGORITHM_BCRYPT = "BCRYPT";

    // 密钥长度
    public static final int RSA_KEY_SIZE = 2048;
    public static final int AES_KEY_SIZE = 256;

    // 密钥过期时间（天）
    public static final int KEY_EXPIRY_DAYS = 90;
    public static final int KEY_CLEANUP_THRESHOLD_DAYS = 30;

    private SecurityConstants() {
        // 常量类，防止实例化
    }

    /**
     * 构建公钥缓存键
     */
    public static String buildPublicKeyKey(String key) {
        return CACHE_PUBLIC_KEY_PREFIX + key;
    }

    /**
     * 构建加密密钥缓存键
     */
    public static String buildEncryptionKeyKey(String business, String version) {
        return CACHE_ENCRYPTION_KEY_PREFIX + business + ":" + version;
    }
}