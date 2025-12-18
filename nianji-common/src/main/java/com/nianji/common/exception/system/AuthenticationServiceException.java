package com.nianji.common.exception.system;

import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.AlarmLevelEnum;
import com.nianji.common.exception.BaseRuntimeException;
import lombok.Getter;

/**
 * 认证服务异常 - 专门处理token生成、验证等认证相关系统错误
 *
 * @author zhangjinming
 */
@Getter
public class AuthenticationServiceException extends SystemException {
    // Getters
    private final String tokenType;
    private final String operation;

    public AuthenticationServiceException(ErrorCode.System errorCode) {
        super(errorCode);
        this.tokenType = null;
        this.operation = null;
    }

    public AuthenticationServiceException(ErrorCode.System errorCode, String message) {
        super(errorCode, message);
        this.tokenType = null;
        this.operation = null;
    }

    public AuthenticationServiceException(ErrorCode.System errorCode, String tokenType, String operation) {
        super(errorCode);
        this.tokenType = tokenType;
        this.operation = operation;
    }

    public AuthenticationServiceException(ErrorCode.System errorCode, String message, String tokenType, String operation) {
        super(errorCode, message);
        this.tokenType = tokenType;
        this.operation = operation;
    }

    public AuthenticationServiceException(ErrorCode.System errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
        this.tokenType = null;
        this.operation = null;
    }

    public AuthenticationServiceException(ErrorCode.System errorCode, String message, String tokenType, String operation, Throwable cause) {
        super(errorCode, message, cause);
        this.tokenType = tokenType;
        this.operation = operation;
    }

    public AuthenticationServiceException(ErrorCode.System errorCode, AlarmLevelEnum alarmLevelEnum) {
        super(errorCode, alarmLevelEnum);
        this.tokenType = null;
        this.operation = null;
    }

    /**
     * 获取认证服务异常详情
     */
    public String getAuthServiceDetail() {
        StringBuilder detail = new StringBuilder();
        if (tokenType != null) {
            detail.append("令牌类型: ").append(tokenType);
        }
        if (operation != null) {
            if (detail.length() > 0) detail.append(", ");
            detail.append("操作: ").append(operation);
        }
        return detail.toString();
    }

    @Override
    public String getFullMessage() {
        String baseMessage = super.getFullMessage();
        String authDetail = getAuthServiceDetail();
        if (!authDetail.isEmpty()) {
            return String.format("%s - %s", baseMessage, authDetail);
        }
        return baseMessage;
    }

}