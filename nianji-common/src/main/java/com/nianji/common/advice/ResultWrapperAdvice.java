package com.nianji.common.advice;

import com.nianji.common.context.CustomRequestContext;
import com.nianji.common.reqres.BizResult;
import com.nianji.common.reqres.Result;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 增强的统一响应包装器 - 集成耗时监控
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.nianji.auth.controller")
public class ResultWrapperAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
                            @NotNull Class<? extends HttpMessageConverter<?>> converterType) {
        // 只处理返回BizResult的方法
        return BizResult.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body, @NotNull MethodParameter returnType,
                                  @NotNull MediaType selectedContentType,
                                  @NotNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response) {

        if (body instanceof BizResult<?> bizResult) {
            Result<?> result = Result.from(bizResult);

            // 记录响应信息（可选，用于调试）
            if (log.isDebugEnabled()) {
                long duration = CustomRequestContext.calculateRequestDuration();
                log.debug("响应包装完成 - 请求ID: {}, 状态: {}, 耗时: {}ms",
                        result.getRequestId(), bizResult.getCode(), duration);
            }

            return result;
        }

        return body;
    }
}