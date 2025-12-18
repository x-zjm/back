package com.nianji.common.reqres;

import com.nianji.common.assertion.ParamAssert;
import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.utils.CommonUtil;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 统一请求模型 - 优化版本 支持多层校验和灵活的校验策略
 */
@Data
public class RequestModel<T> {

    /**
     * 请求ID - 用于链路追踪
     */
    private String requestId;

    /**
     * 请求时间戳 - 用于防重放攻击
     */
    private Long requestTime;

    /**
     * 响应码 - 用于异步响应
     */
    private String responseCode;

    /**
     * 响应描述 - 用于异步响应
     */
    private String responseMsg;

    /**
     * 请求数据 - 使用JSR303校验
     */
    @Valid
    @NotNull(message = "请求参数不能为空")
    private T requestData;

    /**
     * 构建请求模型 - 自动生成请求ID
     */
    public static <T> RequestModel<T> build(T data) {
        RequestModel<T> requestModel = new RequestModel<>();
        requestModel.setRequestId(CommonUtil.generateUUID());
        requestModel.setRequestData(data);
        requestModel.setRequestTime(System.currentTimeMillis());
        return requestModel;
    }

    /**
     * 构建请求模型 - 指定请求ID
     */
    public static <T> RequestModel<T> build(T data, String requestId) {
        RequestModel<T> requestModel = new RequestModel<>();
        requestModel.setRequestId(requestId);
        requestModel.setRequestData(data);
        requestModel.setRequestTime(System.currentTimeMillis());
        return requestModel;
    }

    /**
     * 基础校验 - 校验请求模型完整性
     */
    public void validateBasic() {
        ParamAssert.notBlank(requestId, ErrorCode.Client.PARAM_MISSING, "请求ID不能为空");
        ParamAssert.notNull(requestTime, ErrorCode.Client.PARAM_MISSING, "请求时间不能为空");
        ParamAssert.notNull(requestData, ErrorCode.Client.PARAM_MISSING, "请求数据不能为空");
    }

    /**
     * 防重放校验 - 校验请求时间有效性
     */
    public void validateBasicWithExpire() {
        validateBasic();

        // 校验请求时间应该在5分钟以内
        long now = System.currentTimeMillis();
        long timeDiff = Math.abs(now - requestTime);
        ParamAssert.notNull(
                timeDiff <= 5 * 60 * 1000,
                ErrorCode.Client.REQUEST_EXPIRED
                // String.format("请求已过期，时间差: %dms", timeDiff)
        );
    }

    /**
     * 完整校验 - 包含基础校验和业务数据校验
     */
    public void validateFull() {
        validateBasic();

        // 如果请求数据实现了Validatable接口，调用其校验方法
        if (requestData instanceof Validatable) {
            ((Validatable) requestData).validate();
        }
    }

    /**
     * 完整校验 - 包含基础校验和业务数据校验
     */
    public void validateFullWithExpire() {
        validateBasicWithExpire();

        // 如果请求数据实现了Validatable接口，调用其校验方法
        if (requestData instanceof Validatable) {
            ((Validatable) requestData).validate();
        }
    }

    /**
     * 转换为字符串表示
     */
    @Override
    public String toString() {
        return String.format(
                "RequestModel{requestId='%s', requestTime=%d, data=%s}",
                requestId, requestTime, requestData
        );
    }
}