
package com.nianji.common.exception.client;

import com.nianji.common.errorcode.ErrorCode;

/**
 * 参数校验异常
 *
 * @author zhangjinming
 */
public class ValidationException extends ClientException {
    public ValidationException(ErrorCode.Client errorCode) {
        super(errorCode);
    }

    public ValidationException(ErrorCode.Client errorCode, String message) {
        super(errorCode, message);
    }

    public ValidationException(ErrorCode.Client errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}