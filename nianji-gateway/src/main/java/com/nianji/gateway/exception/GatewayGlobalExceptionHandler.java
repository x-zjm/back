package com.nianji.gateway.exception;

import cn.hutool.json.JSONUtil;
import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.client.AuthenticationException;
import com.nianji.common.exception.client.AuthorizationException;
import com.nianji.common.exception.client.ValidationException;
import com.nianji.common.exception.system.SystemException;
import com.nianji.common.reqres.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 网关全局异常处理器 - 实现ErrorWebExceptionHandler 这个会覆盖Spring Boot的默认错误处理
 */
@Slf4j
@Order(-1) // 最高优先级，在默认处理器之前执行
@Component
public class GatewayGlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        // 如果响应已经提交，直接返回
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 设置默认的HTTP状态码和内容类型
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<Object> result;
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        // 根据异常类型设置相应的HTTP状态码和错误信息
        if (ex instanceof AuthenticationException authEx) {
            result = Result.fail(authEx.getCode(), authEx.getMessage());
            httpStatus = HttpStatus.UNAUTHORIZED;
            log.warn("🔐 认证失败 - 路径: {}, 错误码: {}, 消息: {}",
                    exchange.getRequest().getPath(), authEx.getCode(), authEx.getMessage());

        } else if (ex instanceof AuthorizationException authEx) {
            result = Result.fail(authEx.getCode(), authEx.getMessage());
            httpStatus = HttpStatus.FORBIDDEN;
            log.warn("🚫 授权失败 - 路径: {}, 错误码: {}, 消息: {}",
                    exchange.getRequest().getPath(), authEx.getCode(), authEx.getMessage());

        } else if (ex instanceof ValidationException validationEx) {
            result = Result.fail(validationEx.getCode(), validationEx.getMessage());
            httpStatus = HttpStatus.BAD_REQUEST;
            log.warn("❌ 参数校验失败 - 路径: {}, 错误码: {}, 消息: {}",
                    exchange.getRequest().getPath(), validationEx.getCode(), validationEx.getMessage());

        } else if (ex instanceof SystemException systemEx) {
            result = Result.fail(systemEx.getCode(), systemEx.getMessage());
            log.error("💥 系统异常 - 路径: {}, 错误码: {}, 消息: {}",
                    exchange.getRequest().getPath(), systemEx.getCode(), systemEx.getMessage(), ex);

        } else {
            // 其他未知异常
            result = Result.fail(ErrorCode.System.SYSTEM_ERROR.getCode(), "系统异常，请稍后重试");
            log.error("🚨 未处理异常 - 路径: {}, 异常类型: {}",
                    exchange.getRequest().getPath(), ex.getClass().getSimpleName(), ex);
        }

        // 设置HTTP状态码
        response.setStatusCode(httpStatus);

        // 转换为JSON字符串
        String jsonResult = JSONUtil.toJsonStr(result); // 或者使用JSON序列化工具

        byte[] bytes = jsonResult.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);

        return response.writeWith(Mono.just(buffer));
    }
}