package com.nianji.common.exception.client;

import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.AlarmLevelEnum;
import com.nianji.common.exception.BaseRuntimeException;

/**
 * 客户端异常基类
 *
 * @author zhangjinming
 */
public class ClientException extends BaseRuntimeException {
    public ClientException(ErrorCode.Client errorCode) {
        super(errorCode);
    }

    public ClientException(ErrorCode.Client errorCode, String message) {
        super(errorCode, message);
    }

    public ClientException(ErrorCode.Client errorCode, AlarmLevelEnum alarmLevelEnum) {
        super(errorCode, alarmLevelEnum);
    }

    public ClientException(ErrorCode.Client errorCode, String message, AlarmLevelEnum alarmLevelEnum) {
        super(errorCode, message, alarmLevelEnum);
    }

    public ClientException(ErrorCode.Client errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public ClientException(ErrorCode.Client errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}