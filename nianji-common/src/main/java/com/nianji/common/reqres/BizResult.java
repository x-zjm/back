package com.nianji.common.reqres;

import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.BaseRuntimeException;
import com.nianji.common.exception.ExceptionFactory;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 业务结果 - 用于Service层内部传递，与异常体系深度集成
 */
@Data
@Accessors(chain = true)
public class BizResult<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String SUCCESS_CODE = ErrorCode.Success.SUCCESS.getCode();
    private static final String SUCCESS_MESSAGE = ErrorCode.Success.SUCCESS.getMessage();

    /**
     * 业务状态码
     */
    private String code;

    /**
     * 业务消息
     */
    private String msg;

    /**
     * 业务数据
     */
    private T data;

    /**
     * 业务扩展数据
     */
    private Map<String, Object> extra;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 时间戳
     */
    private long timestamp;

    public BizResult() {
        this.timestamp = System.currentTimeMillis();
    }

    // ==================== 核心静态方法 ====================

    public static <T> BizResult<T> of(String code, String msg, T data, boolean success) {
        BizResult<T> result = new BizResult<>();
        result.code = code;
        result.msg = msg;
        result.data = data;
        result.success = success;
        return result;
    }

    /**
     * 成功结果
     */
    public static <T> BizResult<T> success() {
        return of(SUCCESS_CODE, SUCCESS_MESSAGE, null, true);
    }

    public static <T> BizResult<T> success(T data) {
        return of(SUCCESS_CODE, SUCCESS_MESSAGE, data, true);
    }

    public static <T> BizResult<T> success(T data, String message) {
        return of(SUCCESS_CODE, message, data, true);
    }

    /**
     * 失败结果 - 基于错误码
     */
    public static <T> BizResult<T> fail(ErrorCode errorCode) {
        return of(errorCode.getCode(), errorCode.getMessage(), null, false);
    }

    public static <T> BizResult<T> fail(ErrorCode errorCode, String message) {
        return of(errorCode.getCode(), message, null, false);
    }

    public static <T> BizResult<T> fail(String errorCode, String message) {
        return of(errorCode, message, null, false);
    }

    public static <T> BizResult<T> fail(String errorCode, String message, T data) {
        return of(errorCode, message, data, false);
    }

    /**
     * 从异常创建失败结果
     */
    public static <T> BizResult<T> fail(BaseRuntimeException exception) {
        BizResult<T> result = of(exception.getCode(), exception.getMessage(), null, false);
        // 添加异常相关信息
        result.addExtra("alarmLevel", exception.getAlarmLevelEnum())
              .addExtra("traceId", exception.getTraceId())
              .addExtra("exceptionTime", exception.getTimestamp());
        return result;
    }

    public static <T> BizResult<T> fail(BaseRuntimeException exception, T data) {
        BizResult<T> result = of(exception.getCode(), exception.getMessage(), data, false);
        result.addExtra("alarmLevel", exception.getAlarmLevelEnum())
              .addExtra("traceId", exception.getTraceId())
              .addExtra("exceptionTime", exception.getTimestamp());
        return result;
    }

    // ==================== 便捷方法 ====================

    /**
     * 系统错误
     */
    public static <T> BizResult<T> systemError() {
        return fail(ErrorCode.System.SYSTEM_ERROR);
    }

    public static <T> BizResult<T> systemError(String message) {
        return fail(ErrorCode.System.SYSTEM_ERROR, message);
    }

    /**
     * 数据不存在
     */
    public static <T> BizResult<T> dataNotFound() {
        return fail(ErrorCode.Business.DATA_NOT_FOUND);
    }

    public static <T> BizResult<T> dataNotFound(String message) {
        return fail(ErrorCode.Business.DATA_NOT_FOUND, message);
    }

    /**
     * 参数错误
     */
    public static <T> BizResult<T> paramError() {
        return fail(ErrorCode.Client.PARAM_ERROR);
    }

    public static <T> BizResult<T> paramError(String message) {
        return fail(ErrorCode.Client.PARAM_ERROR, message);
    }

    /**
     * 未授权
     */
    public static <T> BizResult<T> unauthorized() {
        return fail(ErrorCode.Client.UNAUTHORIZED);
    }

    // ==================== 业务便捷方法 ====================

    /**
     * 检查是否成功
     */
    public boolean isSuccess() {
        return success && SUCCESS_CODE.equals(this.code);
    }

    /**
     * 检查是否需要告警
     */
    public boolean needAlarm() {
        return !success && this.extra != null && this.extra.containsKey("alarmLevel");
    }

    /**
     * 获取告警级别
     */
    public Object getAlarmLevel() {
        return this.extra != null ? this.extra.get("alarmLevel") : null;
    }

    /**
     * 获取追踪ID
     */
    public String getTraceId() {
        return this.extra != null ? (String) this.extra.get("traceId") : null;
    }

    // ==================== 链式操作扩展 ====================

    /**
     * 添加扩展信息 - 链式调用
     */
    public BizResult<T> addExtra(String key, Object value) {
        if (this.extra == null) {
            this.extra = new HashMap<>();
        }
        this.extra.put(key, value);
        return this;
    }

    /**
     * 批量设置扩展信息
     */
    public BizResult<T> withExtra(Map<String, Object> extra) {
        this.extra = extra;
        return this;
    }

    /**
     * 设置业务数据 - 链式调用
     */
    public BizResult<T> withData(T data) {
        this.data = data;
        return this;
    }

    /**
     * 设置错误消息 - 链式调用
     */
    public BizResult<T> withMessage(String message) {
        this.msg = message;
        return this;
    }

    // ==================== 调试和监控相关 ====================

    /**
     * 快速创建带调试信息的失败结果
     */
    public static <T> BizResult<T> failWithDebug(ErrorCode errorCode, String debugInfo) {
        BizResult<T> fail = fail(errorCode);
        return fail.addExtra("debugInfo", debugInfo);
    }

    public static <T> BizResult<T> failWithDebug(BaseRuntimeException exception, String debugInfo) {
        BizResult<T> fail = fail(exception);
        return fail.addExtra("debugInfo", debugInfo);
    }

    /**
     * 快速创建带时间戳的失败结果
     */
    public static <T> BizResult<T> failWithTimestamp(ErrorCode errorCode, String message) {
        BizResult<T> fail = fail(errorCode, message);
        return fail.addExtra("serverTime", System.currentTimeMillis());
    }

    /**
     * 创建带性能监控信息的结果
     */
    public static <T> BizResult<T> successWithMetrics(T data, long costTime) {
        return success(data).addExtra("costTime", costTime)
                           .addExtra("metricsRecorded", true);
    }

    // ==================== 转换方法 ====================

    /**
     * 转换为Result对象
     */
    public Result<T> toResult() {
        return Result.from(this);
    }

    /**
     * 如果成功则返回数据，否则抛出异常
     */
    public T getOrThrow() {
        if (isSuccess()) {
            return data;
        }
        throw ExceptionFactory.business(ErrorCode.Business.BUSINESS_ERROR,
                String.format("[%s] %s", code, msg));
    }

    @Override
    public String toString() {
        return String.format("BizResult{code='%s', success=%s, msg='%s', data=%s}",
                code, success, msg, data);
    }
}