package com.nianji.common.assertion;

import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.ExceptionFactory;
import com.nianji.common.reqres.Result;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 业务断言工具类
 *
 * @author zhangjinming
 */
public final class BusinessAssert {

    private BusinessAssert() {
    }

    // ========== 业务断言 ==========

    public static void isTrue(boolean expression, ErrorCode.Business errorCode) {
        if (!expression) {
            throw ExceptionFactory.business(errorCode);
        }
    }

    public static void isTrue(boolean expression, ErrorCode.Business errorCode, String message) {
        if (!expression) {
            throw ExceptionFactory.business(errorCode, message);
        }
    }

    public static void isTrue(boolean expression, ErrorCode.Business errorCode, Supplier<String> messageSupplier) {
        if (!expression) {
            throw ExceptionFactory.business(errorCode, messageSupplier.get());
        }
    }

    public static <T> T notNull(T object, ErrorCode.Business errorCode) {
        if (Objects.isNull(object)) {
            throw ExceptionFactory.business(errorCode);
        }
        return object;
    }

    public static <T> T notNull(T object, ErrorCode.Business errorCode, String message) {
        if (Objects.isNull(object)) {
            throw ExceptionFactory.business(errorCode, message);
        }
        return object;
    }

    public static <T> Collection<T> notEmpty(Collection<T> collection, ErrorCode.Business errorCode) {
        if (collection == null || collection.isEmpty()) {
            throw ExceptionFactory.business(errorCode);
        }
        return collection;
    }

    public static <T> Collection<T> notEmpty(Collection<T> collection, ErrorCode.Business errorCode, String message) {
        if (collection == null || collection.isEmpty()) {
            throw ExceptionFactory.business(errorCode, message);
        }
        return collection;
    }

    public static <K, V> Map<K, V> notEmpty(Map<K, V> map, ErrorCode.Business errorCode) {
        if (map == null || map.isEmpty()) {
            throw ExceptionFactory.business(errorCode);
        }
        return map;
    }

    public static <K, V> Map<K, V> notEmpty(Map<K, V> map, ErrorCode.Business errorCode, String message) {
        if (map == null || map.isEmpty()) {
            throw ExceptionFactory.business(errorCode, message);
        }
        return map;
    }

    public static String notBlank(String string, ErrorCode.Business errorCode) {
        if (string == null || string.trim().isEmpty()) {
            throw ExceptionFactory.business(errorCode);
        }
        return string;
    }

    public static String notBlank(String string, ErrorCode.Business errorCode, String message) {
        if (string == null || string.trim().isEmpty()) {
            throw ExceptionFactory.business(errorCode, message);
        }
        return string;
    }

    // ========== Result相关断言方法 ==========

    /**
     * 检查Result是否成功，如果失败则抛出业务异常
     */
    public static <T> T checkResultSuccess(Result<T> result, ErrorCode.Business errorCode) {
        if (!result.isSuccess()) {
            throw ExceptionFactory.business(errorCode, result.getMsg());
        }
        return result.getData();
    }

    /**
     * 检查Result是否成功，如果失败则抛出业务异常（带自定义消息）
     */
    public static <T> T checkResultSuccess(Result<T> result, ErrorCode.Business errorCode, String message) {
        if (!result.isSuccess()) {
            throw ExceptionFactory.business(errorCode, message);
        }
        return result.getData();
    }

    /**
     * 检查Result是否成功，如果失败则返回失败的Result
     */
    public static <T> Result<T> requireResultSuccess(Result<T> result) {
        if (!result.isSuccess()) {
            return Result.fail(result.getCode(), result.getMsg());
        }
        return result;
    }
}