package com.nianji.common.exception.business;

import com.nianji.common.errorcode.ErrorCode;

/**
 * 数据不存在异常 用于查询数据不存在场景
 *
 * @author zhangjinming
 */
public class DataNotFoundException extends BusinessException {
    public DataNotFoundException(ErrorCode.Business errorCode) {
        super(errorCode);
    }

    public DataNotFoundException(ErrorCode.Business errorCode, String message) {
        super(errorCode, message);
    }

    public DataNotFoundException(ErrorCode.Business errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}