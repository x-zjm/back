package com.nianji.common.exception.client;

import com.nianji.common.errorcode.ErrorCode;

/**
 * 授权异常
 *
 * @author zhangjinming
 */
public class AuthorizationException extends ClientException {
    public AuthorizationException(ErrorCode.Client errorCode) {
        super(errorCode);
    }

    public AuthorizationException(ErrorCode.Client errorCode, String message) {
        super(errorCode, message);
    }

    public AuthorizationException(ErrorCode.Client errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
