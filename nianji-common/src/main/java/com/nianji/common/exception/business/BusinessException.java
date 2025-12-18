package com.nianji.common.exception.business;


import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.AlarmLevelEnum;
import com.nianji.common.exception.BaseRuntimeException;

/**
 * 业务异常
 *
 * @author zhangjinming
 */
public class BusinessException extends BaseRuntimeException {
    public BusinessException(ErrorCode.Business errorCode) {
        super(errorCode);
    }

    public BusinessException(ErrorCode.Business errorCode, String message) {
        super(errorCode, message);
    }

    public BusinessException(ErrorCode.Business errorCode, AlarmLevelEnum alarmLevelEnum) {
        super(errorCode, alarmLevelEnum);
    }

    public BusinessException(ErrorCode.Business errorCode, String message, AlarmLevelEnum alarmLevelEnum) {
        super(errorCode, message, alarmLevelEnum);
    }

    public BusinessException(ErrorCode.Business errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public BusinessException(ErrorCode.Business errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public BusinessException(String errorCode, String message) {
        super(errorCode, message);
    }
}