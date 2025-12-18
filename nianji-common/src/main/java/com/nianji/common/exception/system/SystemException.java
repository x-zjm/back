package com.nianji.common.exception.system;

import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.AlarmLevelEnum;
import com.nianji.common.exception.BaseRuntimeException;

/**
 * 系统异常 用于系统级别错误，对应HTTP 500状态码
 *
 * @author zhangjinming
 */
public class SystemException extends BaseRuntimeException {
    public SystemException(ErrorCode.System errorCode) {
        super(errorCode);
    }

    public SystemException(ErrorCode.System errorCode, String message) {
        super(errorCode, message);
    }

    public SystemException(ErrorCode.System errorCode, AlarmLevelEnum alarmLevelEnum) {
        super(errorCode, alarmLevelEnum);
    }

    public SystemException(ErrorCode.System errorCode, String message, AlarmLevelEnum alarmLevelEnum) {
        super(errorCode, message, alarmLevelEnum);
    }

    public SystemException(ErrorCode.System errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public SystemException(ErrorCode.System errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}