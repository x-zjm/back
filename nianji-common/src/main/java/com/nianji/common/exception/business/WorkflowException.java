package com.nianji.common.exception.business;

import com.nianji.common.errorcode.ErrorCode;

/**
 * 工作流异常
 *
 * @author zhangjinming
 */
public class WorkflowException extends BusinessException {
    public WorkflowException(ErrorCode.Business errorCode) {
        super(errorCode);
    }

    public WorkflowException(ErrorCode.Business errorCode, String message) {
        super(errorCode, message);
    }

    public WorkflowException(ErrorCode.Business errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}