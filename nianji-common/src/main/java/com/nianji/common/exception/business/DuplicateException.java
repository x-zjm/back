package com.nianji.common.exception.business;

import com.nianji.common.errorcode.ErrorCode;

/**
 * 重复数据异常 用于数据唯一性约束违反场景
 *
 * @author zhangjinming
 */
public class DuplicateException extends BusinessException {
    public DuplicateException(ErrorCode.Business errorCode) {
        super(errorCode);
    }

    public DuplicateException(ErrorCode.Business errorCode, String message) {
        super(errorCode, message);
    }

    public DuplicateException(ErrorCode.Business errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}