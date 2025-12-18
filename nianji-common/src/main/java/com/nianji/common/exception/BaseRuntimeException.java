package com.nianji.common.exception;

import com.nianji.common.errorcode.ErrorCode;
import lombok.Getter;

/**
 * 基础运行时异常 企业级异常基类，集成错误码和告警等级
 *
 * @author zhangjinming
 */
@Getter
public class BaseRuntimeException extends RuntimeException {
    private final String code;
    private final String message;
    private final AlarmLevelEnum alarmLevelEnum;
    private final boolean loggable;
    private final long timestamp;

    public BaseRuntimeException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.alarmLevelEnum = AlarmLevelEnum.fromErrorCode(errorCode.getCode());
        this.loggable = true;
        this.timestamp = System.currentTimeMillis();
    }

    public BaseRuntimeException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.message = message;
        this.alarmLevelEnum = AlarmLevelEnum.fromErrorCode(errorCode.getCode());
        this.loggable = true;
        this.timestamp = System.currentTimeMillis();
    }

    public BaseRuntimeException(String errorCode, String message) {
        super(message);
        this.code = errorCode;
        this.message = message;
        this.alarmLevelEnum = AlarmLevelEnum.fromErrorCode(errorCode);
        this.loggable = true;
        this.timestamp = System.currentTimeMillis();
    }

    public BaseRuntimeException(ErrorCode errorCode, AlarmLevelEnum alarmLevelEnum) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.alarmLevelEnum = alarmLevelEnum;
        this.loggable = true;
        this.timestamp = System.currentTimeMillis();
    }

    public BaseRuntimeException(ErrorCode errorCode, String message, AlarmLevelEnum alarmLevelEnum) {
        super(message);
        this.code = errorCode.getCode();
        this.message = message;
        this.alarmLevelEnum = alarmLevelEnum;
        this.loggable = true;
        this.timestamp = System.currentTimeMillis();
    }

    public BaseRuntimeException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.alarmLevelEnum = AlarmLevelEnum.fromErrorCode(errorCode.getCode());
        this.loggable = true;
        this.timestamp = System.currentTimeMillis();
    }

    public BaseRuntimeException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.code = errorCode.getCode();
        this.message = message;
        this.alarmLevelEnum = AlarmLevelEnum.fromErrorCode(errorCode.getCode());
        this.loggable = true;
        this.timestamp = System.currentTimeMillis();
    }

    public BaseRuntimeException(ErrorCode errorCode, String message, Throwable cause, AlarmLevelEnum alarmLevelEnum) {
        super(message, cause);
        this.code = errorCode.getCode();
        this.message = message;
        this.alarmLevelEnum = alarmLevelEnum;
        this.loggable = true;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 获取完整的错误信息
     */
    public String getFullMessage() {
        return String.format("[%s] %s (告警级别: %s)", code, message, alarmLevelEnum.getDescription());
    }

    /**
     * 获取错误追踪ID（可用于日志追踪）
     */
    public String getTraceId() {
        return String.format("%s-%d", code, timestamp);
    }

    @Override
    public String toString() {
        return String.format("BaseRuntimeException{code='%s', message='%s', alarmLevel=%s, timestamp=%d}",
                code, message, alarmLevelEnum, timestamp);
    }
}