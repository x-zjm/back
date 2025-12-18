package com.nianji.common.exception.system;

import com.nianji.common.errorcode.ErrorCode;

/**
 * 数据库异常 用于数据库操作失败场景
 *
 * @author zhangjinming
 */
public class DatabaseException extends SystemException {
    public DatabaseException(ErrorCode.System errorCode) {
        super(errorCode);
    }

    public DatabaseException(ErrorCode.System errorCode, String message) {
        super(errorCode, message);
    }

    public DatabaseException(ErrorCode.System errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}