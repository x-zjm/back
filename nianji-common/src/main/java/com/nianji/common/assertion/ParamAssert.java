package com.nianji.common.assertion;

import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.ExceptionFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 参数校验断言工具类 - 专门用于请求参数校验 抛出ValidationException，对应HTTP 400状态码
 */
public final class ParamAssert {

    private ParamAssert() {
    }

    // ========== 基础参数校验 ==========

    public static void isTrue(boolean expression, ErrorCode.Client errorCode) {
        if (!expression) {
            throw ExceptionFactory.validation(errorCode);
        }
    }

    public static void isTrue(boolean expression, ErrorCode.Client errorCode, String message) {
        if (!expression) {
            throw ExceptionFactory.validation(errorCode, message);
        }
    }

    public static void isTrue(boolean expression, ErrorCode.Client errorCode, Supplier<String> messageSupplier) {
        if (!expression) {
            throw ExceptionFactory.validation(errorCode, messageSupplier.get());
        }
    }

    public static <T> T notNull(T object, ErrorCode.Client errorCode) {
        if (Objects.isNull(object)) {
            throw ExceptionFactory.validation(errorCode);
        }
        return object;
    }

    public static <T> T notNull(T object, ErrorCode.Client errorCode, String message) {
        if (Objects.isNull(object)) {
            throw ExceptionFactory.validation(errorCode, message);
        }
        return object;
    }

    public static String notBlank(String string, ErrorCode.Client errorCode) {
        if (string == null || string.trim().isEmpty()) {
            throw ExceptionFactory.validation(errorCode);
        }
        return string;
    }

    public static String notBlank(String string, ErrorCode.Client errorCode, String message) {
        if (string == null || string.trim().isEmpty()) {
            throw ExceptionFactory.validation(errorCode, message);
        }
        return string;
    }

    public static <T> Collection<T> notEmpty(Collection<T> collection, ErrorCode.Client errorCode) {
        if (collection == null || collection.isEmpty()) {
            throw ExceptionFactory.validation(errorCode);
        }
        return collection;
    }

    public static <T> Collection<T> notEmpty(Collection<T> collection, ErrorCode.Client errorCode, String message) {
        if (collection == null || collection.isEmpty()) {
            throw ExceptionFactory.validation(errorCode, message);
        }
        return collection;
    }

    public static <K, V> Map<K, V> notEmpty(Map<K, V> map, ErrorCode.Client errorCode) {
        if (map == null || map.isEmpty()) {
            throw ExceptionFactory.validation(errorCode);
        }
        return map;
    }

    // ========== 特定场景参数校验 ==========

    /**
     * 邮箱格式校验
     */
    public static String validEmail(String email, ErrorCode.Client errorCode) {
        notBlank(email, errorCode, "邮箱不能为空");
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw ExceptionFactory.validation(errorCode, "邮箱格式不正确");
        }
        return email;
    }

    /**
     * 手机号格式校验
     */
    public static String validPhone(String phone, ErrorCode.Client errorCode) {
        notBlank(phone, errorCode, "手机号不能为空");
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw ExceptionFactory.validation(errorCode, "手机号格式不正确");
        }
        return phone;
    }

    /**
     * 长度校验
     */
    public static String validLength(String str, int min, int max, ErrorCode.Client errorCode) {
        notBlank(str, errorCode);
        if (str.length() < min || str.length() > max) {
            throw ExceptionFactory.validation(errorCode,
                    String.format("长度必须在%d-%d个字符之间", min, max));
        }
        return str;
    }

    /**
     * 数值范围校验
     */
    public static <T extends Number & Comparable<T>> T validRange(T value, T min, T max, ErrorCode.Client errorCode) {
        notNull(value, errorCode);
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw ExceptionFactory.validation(errorCode,
                    String.format("数值必须在%s-%s之间", min, max));
        }
        return value;
    }
}