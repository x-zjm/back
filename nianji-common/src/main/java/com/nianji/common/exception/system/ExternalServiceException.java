package com.nianji.common.exception.system;

import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.AlarmLevelEnum;
import com.nianji.common.exception.BaseRuntimeException;
import lombok.Getter;

/**
 * 外部服务异常
 *
 * @author zhangjinming
 */
@Getter
public class ExternalServiceException extends BaseRuntimeException {
    private final String serviceName;
    private final String serviceUrl;

    public ExternalServiceException(ErrorCode.ThirdParty errorCode, String serviceName) {
        super(errorCode, AlarmLevelEnum.HIGH);
        this.serviceName = serviceName;
        this.serviceUrl = null;
    }

    public ExternalServiceException(ErrorCode.ThirdParty errorCode, String serviceName, String message) {
        super(errorCode, message, AlarmLevelEnum.HIGH);
        this.serviceName = serviceName;
        this.serviceUrl = null;
    }

    public ExternalServiceException(ErrorCode.ThirdParty errorCode, String serviceName, String serviceUrl, String message) {
        super(errorCode, message, AlarmLevelEnum.HIGH);
        this.serviceName = serviceName;
        this.serviceUrl = serviceUrl;
    }

    public ExternalServiceException(ErrorCode.ThirdParty errorCode, String serviceName, Throwable cause) {
        super(errorCode, cause);
        this.serviceName = serviceName;
        this.serviceUrl = null;
    }

    public ExternalServiceException(ErrorCode.ThirdParty errorCode, String serviceName, String message, Throwable cause) {
        super(errorCode, message, cause);
        this.serviceName = serviceName;
        this.serviceUrl = null;
    }

    public ExternalServiceException(ErrorCode.ThirdParty errorCode, String serviceName, String serviceUrl, String message, Throwable cause) {
        super(errorCode, message, cause);
        this.serviceName = serviceName;
        this.serviceUrl = serviceUrl;
    }

    @Override
    public String getFullMessage() {
        String baseMessage = super.getFullMessage();
        if (serviceUrl != null) {
            return String.format("%s - 服务: %s (%s)", baseMessage, serviceName, serviceUrl);
        }
        return String.format("%s - 服务: %s", baseMessage, serviceName);
    }
}