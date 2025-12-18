package com.nianji.common.utils;


import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.ExceptionFactory;
import lombok.extern.slf4j.Slf4j;


import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


/**
 * 通用工具类 提供各种常用的工具方法
 *
 * @author zhangjinming
 * @version 1.0.0
 */
@Slf4j
public class CommonUtil {


    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String NUMBERS = "0123456789";
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String SECURE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}|;:,.<>?";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");


    /**
     * 生成唯一ID（基于Snowflake算法或时间戳+随机数）
     *
     * @return 唯一ID
     */
    public static Long generateId() {
        // 使用时间戳 + 随机数生成唯一ID
        // 在实际项目中，建议使用Snowflake算法或数据库自增ID
        long timestamp = System.currentTimeMillis();
        long random = ThreadLocalRandom.current().nextLong(1000, 9999);
        return timestamp * 10000 + random;
    }


    /**
     * 生成UUID（不含横线）
     *
     * @return 32位UUID字符串
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }


    /**
     * 生成带横线的标准UUID
     *
     * @return 标准UUID字符串
     */
    public static String generateStandardUUID() {
        return UUID.randomUUID().toString();
    }


    /**
     * 生成随机字符串
     *
     * @param length
     *         字符串长度
     * @return 随机字符串
     */
    public static String generateRandomString(int length) {
        return generateRandomString(length, ALPHANUMERIC);
    }


    /**
     * 生成安全的随机字符串（包含特殊字符）
     *
     * @param length
     *         字符串长度
     * @return 安全随机字符串
     */
    public static String generateSecureRandomString(int length) {
        return generateRandomString(length, SECURE_CHARACTERS);
    }


    /**
     * 生成数字随机字符串
     *
     * @param length
     *         字符串长度
     * @return 数字随机字符串
     */
    public static String generateRandomNumber(int length) {
        return generateRandomString(length, NUMBERS);
    }


    /**
     * 生成指定字符集的随机字符串
     *
     * @param length
     *         字符串长度
     * @param characters
     *         字符集
     * @return 随机字符串
     */
    public static String generateRandomString(int length, String characters) {
        if (length <= 0) {
            log.error("工具类参数错误: 长度必须大于0, 实际值: {}", length);
            throw ExceptionFactory.system(
                    ErrorCode.System.CONFIG_ERROR,
                    "长度必须大于0"
            );
        }
        if (characters == null || characters.isEmpty()) {
            log.error("字符集不能为空");
            throw ExceptionFactory.system(
                    ErrorCode.System.CONFIG_ERROR,
                    "字符集不能为空"
            );
        }


        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = SECURE_RANDOM.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }


    /**
     * 生成验证码（6位数字）
     *
     * @return 6位数字验证码
     */
    public static String generateVerificationCode() {
        return generateRandomNumber(6);
    }


    /**
     * 生成时间戳ID（格式：yyyyMMddHHmmssSSS + 3位随机数）
     *
     * @return 时间戳ID
     */
    public static String generateTimestampId() {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String random = generateRandomNumber(3);
        return timestamp + random;
    }


    /**
     * 生成订单号（格式：yyyyMMddHHmmss + 6位随机数）
     *
     * @return 订单号
     */
    public static String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = generateRandomNumber(6);
        return timestamp + random;
    }


    /**
     * 生成邀请码（8位字母数字）
     *
     * @return 邀请码
     */
    public static String generateInviteCode() {
        return generateRandomString(8).toUpperCase();
    }


    /**
     * 生成短链接码（6-8位字母数字）
     *
     * @return 短链接码
     */
    public static String generateShortCode() {
        return generateRandomString(6 + SECURE_RANDOM.nextInt(3));
    }


    /**
     * 掩码处理手机号
     *
     * @param phone
     *         手机号
     * @return 掩码后的手机号
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }


    /**
     * 掩码处理邮箱
     *
     * @param email
     *         邮箱
     * @return 掩码后的邮箱
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }


        int atIndex = email.indexOf("@");
        String prefix = email.substring(0, atIndex);
        String domain = email.substring(atIndex);


        if (prefix.length() <= 2) {
            return prefix.charAt(0) + "***" + domain;
        } else {
            return prefix.substring(0, 2) + "***" + domain;
        }
    }


    /**
     * 掩码处理身份证号
     *
     * @param idCard
     *         身份证号
     * @return 掩码后的身份证号
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }


    /**
     * 检查字符串是否为空
     *
     * @param str
     *         字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }


    /**
     * 检查字符串是否不为空
     *
     * @param str
     *         字符串
     * @return 是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }


    /**
     * 安全的字符串比较
     *
     * @param str1
     *         字符串1
     * @param str2
     *         字符串2
     * @return 是否相等
     */
    public static boolean safeEquals(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equals(str2);
    }


    /**
     * 截取字符串，如果超过指定长度则添加省略号
     *
     * @param str
     *         字符串
     * @param length
     *         最大长度
     * @return 截取后的字符串
     */
    public static String truncate(String str, int length) {
        if (str == null || str.length() <= length) {
            return str;
        }
        return str.substring(0, length) + "...";
    }


    /**
     * 生成文件名的随机名称
     *
     * @param originalFileName
     *         原始文件名
     * @return 随机文件名
     */
    public static String generateRandomFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return generateTimestampId() + extension;
    }


    /**
     * 生成密码盐值
     *
     * @return 16位随机盐值
     */
    public static String generateSalt() {
        return generateSecureRandomString(16);
    }


    /**
     * 计算分页偏移量
     *
     * @param page
     *         页码（从1开始）
     * @param size
     *         每页大小
     * @return 偏移量
     */
    public static int calculateOffset(int page, int size) {
        if (page < 1) {
            page = 1;
        }
        return (page - 1) * size;
    }


    /**
     * 生成用户默认昵称
     *
     * @param prefix
     *         前缀
     * @return 默认昵称
     */
    public static String generateDefaultNickname(String prefix) {
        return prefix + generateRandomNumber(8);
    }


    /**
     * 生成API密钥
     *
     * @return 32位API密钥
     */
    public static String generateApiKey() {
        return generateSecureRandomString(32);
    }


    /**
     * 生成访问令牌
     *
     * @return 访问令牌
     */
    public static String generateAccessToken() {
        return generateUUID();
    }


    /**
     * 生成刷新令牌
     *
     * @return 刷新令牌
     */
    public static String generateRefreshToken() {
        return generateSecureRandomString(64);
    }


    /**
     * 格式化文件大小
     *
     * @param size
     *         文件大小（字节）
     * @return 格式化后的文件大小
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }


    /**
     * 生成颜色代码
     *
     * @return 16进制颜色代码
     */
    public static String generateColorCode() {
        Random random = new Random();
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return String.format("#%02X%02X%02X", r, g, b);
    }


    /**
     * 生成Gravatar邮箱哈希
     *
     * @param email
     *         邮箱地址
     * @return MD5哈希值
     */
    public static String generateGravatarHash(String email) {
        if (isEmpty(email)) {
            return "";
        }
        String trimmedEmail = email.trim().toLowerCase();
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(trimmedEmail.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(Integer.toHexString((b & 0xFF) | 0x100), 1, 3);
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            log.error("生成Gravatar哈希失败", e);
            return "";
        }
    }


    /**
     * 生成简单的哈希码（用于简单的分布）
     *
     * @param str
     *         字符串
     * @return 哈希码
     */
    public static int simpleHash(String str) {
        if (isEmpty(str)) {
            return 0;
        }
        int hash = 0;
        for (int i = 0; i < str.length(); i++) {
            hash = 31 * hash + str.charAt(i);
        }
        return Math.abs(hash);
    }


    /**
     * 生成分布式锁的键
     *
     * @param prefix
     *         前缀
     * @param key
     *         键
     * @return 完整的锁键
     */
    public static String generateLockKey(String prefix, String key) {
        return "lock:" + prefix + ":" + key;
    }


    /**
     * 生成缓存键
     *
     * @param prefix
     *         前缀
     * @param key
     *         键
     * @return 完整的缓存键
     */
    public static String generateCacheKey(String prefix, String key) {
        return "cache:" + prefix + ":" + key;
    }


    /**
     * 生成限流键
     *
     * @param prefix
     *         前缀
     * @param key
     *         键
     * @return 完整的限流键
     */
    public static String generateRateLimitKey(String prefix, String key) {
        return "rate:" + prefix + ":" + key;
    }


    /**
     * 生成会话ID
     *
     * @return 会话ID
     */
    public static String generateSessionId() {
        return "session:" + generateUUID();
    }

    /**
     * 安全的类型转换方法
     *
     * @param str
     *         String格式数字
     * @return Long类型数字
     */
    public static Long convertToLong(String str) {
        if (str == null) {
            return null;
        }
        try {
            return Long.valueOf(str);
        } catch (NumberFormatException e) {
            log.warn("无法将字符串转换为Long: {}", str);
            return null;
        }
    }
}