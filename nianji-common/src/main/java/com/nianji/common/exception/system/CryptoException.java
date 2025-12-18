package com.nianji.common.exception.system;

import com.nianji.common.errorcode.ErrorCode;

/**
 * 加密异常 用于加密解密操作失败场景
 *
 * @author zhangjinming
 */
public class CryptoException extends SystemException {
    public CryptoException(ErrorCode.System errorCode) {
        super(errorCode);
    }

    public CryptoException(ErrorCode.System errorCode, String message) {
        super(errorCode, message);
    }

    public CryptoException(ErrorCode.System errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}