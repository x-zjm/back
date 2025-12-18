
package com.nianji.common.exception.client;

import com.nianji.common.errorcode.ErrorCode;

/**
 * 认证异常
 *
 * @author zhangjinming
 */
public class AuthenticationException extends ClientException {
    public AuthenticationException(ErrorCode.Client errorCode) {
        super(errorCode);
    }

    public AuthenticationException(ErrorCode.Client errorCode, String message) {
        super(errorCode, message);
    }

    public AuthenticationException(ErrorCode.Client errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}