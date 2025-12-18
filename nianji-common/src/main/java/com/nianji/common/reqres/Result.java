package com.nianji.common.reqres;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.nianji.common.context.CustomRequestContext;
import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.BaseRuntimeException;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP响应封装 - 与异常和监控体系深度集成
 */
@Data
@Accessors(chain = true)
public class Result<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String SUCCESS_CODE = ErrorCode.Success.SUCCESS.getCode();
    private static final String SUCCESS_MESSAGE = ErrorCode.Success.SUCCESS.getMessage();

    /**
     * 请求流水号
     */
    private String requestId;

    /**
     * 响应时间
     */
    private String responseTime;

    /**
     * 响应结果编码
     */
    private String code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 服务器处理耗时(ms)
     */
    private Long costTime;

    /**
     * 扩展信息
     */
    private Object extra;

    private Result() {
        this.responseTime = DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATETIME_PATTERN);
        this.requestId = CustomRequestContext.getRequestId();
        // 计算处理耗时
        this.costTime = CustomRequestContext.calculateRequestDuration();
    }

    private Result(String code, String msg, T data, boolean success) {
        this();
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.success = success;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>(SUCCESS_CODE, SUCCESS_MESSAGE, null, true);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, SUCCESS_MESSAGE, data, true);
    }

    public static <T> Result<T> success(T data, String message) {
        return new Result<>(SUCCESS_CODE, message, data, true);
    }

    /**
     * 失败响应 - 基于错误码
     */
    public static <T> Result<T> fail(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null, false);
    }

    public static <T> Result<T> fail(ErrorCode errorCode, String message) {
        return new Result<>(errorCode.getCode(), message, null, false);
    }

    public static <T> Result<T> fail(String errorCode, String message) {
        return new Result<>(errorCode, message, null, false);
    }

    public static <T> Result<T> fail(String errorCode, String message, T data) {
        return new Result<>(errorCode, message, data, false);
    }

    /**
     * 从异常创建失败响应
     */
    public static <T> Result<T> fail(BaseRuntimeException exception) {
        Result<T> result = new Result<>(exception.getCode(), exception.getMessage(), null, false);
        // 携带异常额外信息
        result.extra = createExceptionExtra(exception);
        return result;
    }

    public static <T> Result<T> fail(BaseRuntimeException exception, T data) {
        Result<T> result = new Result<>(exception.getCode(), exception.getMessage(), data, false);
        result.extra = createExceptionExtra(exception);
        return result;
    }

    /**
     * 从BizResult转换
     */
    public static <T> Result<T> from(BizResult<T> bizResult) {
        if (bizResult == null) {
            return fail(ErrorCode.System.SYSTEM_ERROR, "业务结果为空");
        }

        Result<T> result = new Result<>();
        result.code = bizResult.getCode();
        result.msg = bizResult.getMsg();
        result.data = bizResult.getData();
        result.success = bizResult.isSuccess();

        // 携带BizResult的扩展信息
        if (bizResult.getExtra() != null && !bizResult.getExtra().isEmpty()) {
            result.extra = bizResult.getExtra();
        }

        return result;
    }

    // ==================== 便捷方法 ====================

    /**
     * 系统错误响应
     */
    public static <T> Result<T> systemError() {
        return fail(ErrorCode.System.SYSTEM_ERROR);
    }

    public static <T> Result<T> systemError(String message) {
        return fail(ErrorCode.System.SYSTEM_ERROR, message);
    }

    /**
     * 参数错误响应
     */
    public static <T> Result<T> paramError() {
        return fail(ErrorCode.Client.PARAM_ERROR);
    }

    public static <T> Result<T> paramError(String message) {
        return fail(ErrorCode.Client.PARAM_ERROR, message);
    }

    /**
     * 数据不存在响应
     */
    public static <T> Result<T> dataNotFound() {
        return fail(ErrorCode.Business.DATA_NOT_FOUND);
    }

    public static <T> Result<T> dataNotFound(String message) {
        return fail(ErrorCode.Business.DATA_NOT_FOUND, message);
    }

    /**
     * 未授权响应
     */
    public static <T> Result<T> unauthorized() {
        return fail(ErrorCode.Client.UNAUTHORIZED);
    }

    /**
     * 权限不足响应
     */
    public static <T> Result<T> forbidden() {
        return fail(ErrorCode.Client.FORBIDDEN);
    }

    // ==================== 工具方法 ====================

    /**
     * 检查是否成功
     */
    public boolean isSuccess() {
        return success && SUCCESS_CODE.equals(this.code);
    }

    /**
     * 获取处理耗时
     */
    public Long getCostTime() {
        return costTime != null ? costTime : 0L;
    }

    /**
     * 设置扩展信息
     */
    public Result<T> withExtra(Object extra) {
        this.extra = extra;
        return this;
    }

    /**
     * 设置处理耗时
     */
    public Result<T> withCostTime(Long costTime) {
        this.costTime = costTime;
        return this;
    }

    // ==================== 私有方法 ====================

    /**
     * 创建异常额外信息
     */
    private static Map<String, Object> createExceptionExtra(BaseRuntimeException exception) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("alarmLevel", exception.getAlarmLevelEnum().name());
        extra.put("traceId", exception.getTraceId());
        extra.put("exceptionTime", exception.getTimestamp());
        extra.put("exceptionType", exception.getClass().getSimpleName());
        return extra;
    }

    @Override
    public String toString() {
        return String.format(
                "Result{requestId='%s', code='%s', success=%s, msg='%s', costTime=%dms}",
                requestId, code, success, msg, getCostTime()
        );
    }
}