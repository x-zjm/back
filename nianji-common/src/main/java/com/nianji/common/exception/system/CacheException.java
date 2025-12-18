package com.nianji.common.exception.system;

import com.nianji.common.errorcode.ErrorCode;

/**
 * 缓存异常
 *
 * @author zhangjinming
 */
public class CacheException extends SystemException {
    public CacheException(ErrorCode.System errorCode) {
        super(errorCode);
    }

    public CacheException(ErrorCode.System errorCode, String message) {
        super(errorCode, message);
    }

    public CacheException(ErrorCode.System errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}