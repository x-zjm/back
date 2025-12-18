package com.nianji.common.exception.global;

import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.*;
import com.nianji.common.exception.business.BusinessException;
import com.nianji.common.exception.business.DataNotFoundException;
import com.nianji.common.exception.business.DuplicateException;
import com.nianji.common.exception.client.*;
import com.nianji.common.exception.system.*;
import com.nianji.common.reqres.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;

import java.util.stream.Collectors;

/**
 * 全局异常处理器 - 基于Result响应类优化
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Object>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        logException(e, request);
        return buildResponseEntity(e, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Result<Object>> handleValidationException(ValidationException e, HttpServletRequest request) {
        logException(e, request);
        return buildResponseEntity(e, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result<Object>> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        logException(e, request);
        return buildResponseEntity(e, HttpStatus.UNAUTHORIZED);
    }

    /**
     * 处理授权异常
     */
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<Result<Object>> handleAuthorizationException(AuthorizationException e, HttpServletRequest request) {
        logException(e, request);
        return buildResponseEntity(e, HttpStatus.FORBIDDEN);
    }

    /**
     * 处理限流异常
     */
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<Result<Object>> handleRateLimitException(RateLimitException e, HttpServletRequest request) {
        logException(e, request);

        // 创建限流响应结果
        Result<Object> result = Result.fail(e.getCode(), e.getMessage());

        // 添加限流相关信息到响应头
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("X-RateLimit-Limit", String.valueOf(e.getLimitCount()))
                .header("X-RateLimit-Remaining", String.valueOf(e.getRemaining()))
                .header("X-RateLimit-Reset", String.valueOf(e.getResetTime()))
                .body(result);
    }

    /**
     * 处理数据不存在异常
     */
    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<Result<Object>> handleDataNotFoundException(DataNotFoundException e, HttpServletRequest request) {
        logException(e, request);
        return buildResponseEntity(e, HttpStatus.NOT_FOUND);
    }

    /**
     * 处理重复数据异常
     */
    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<Result<Object>> handleDuplicateException(DuplicateException e, HttpServletRequest request) {
        logException(e, request);
        return buildResponseEntity(e, HttpStatus.CONFLICT);
    }

    /**
     * 处理系统异常
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<Result<Object>> handleSystemException(SystemException e, HttpServletRequest request) {
        logException(e, request);
        return buildResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 处理数据库异常
     */
    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<Result<Object>> handleDatabaseException(DatabaseException e, HttpServletRequest request) {
        logException(e, request);
        return buildResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 处理缓存异常
     */
    @ExceptionHandler(CacheException.class)
    public ResponseEntity<Result<Object>> handleCacheException(CacheException e, HttpServletRequest request) {
        logException(e, request);
        return buildResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 处理加密异常
     */
    @ExceptionHandler(CryptoException.class)
    public ResponseEntity<Result<Object>> handleCryptoException(CryptoException e, HttpServletRequest request) {
        logException(e, request);
        return buildResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 处理外部服务异常
     */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<Result<Object>> handleExternalServiceException(ExternalServiceException e, HttpServletRequest request) {
        logException(e, request);
        return buildResponseEntity(e, HttpStatus.BAD_GATEWAY);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Object>> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        ValidationException exception = ExceptionFactory.validation(ErrorCode.Client.PARAM_ERROR, message);
        logException(exception, request);
        return buildResponseEntity(exception, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理方法参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        ValidationException exception = ExceptionFactory.validation(ErrorCode.Client.PARAM_ERROR, message);
        logException(exception, request);
        return buildResponseEntity(exception, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Object>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        ValidationException exception = ExceptionFactory.validation(ErrorCode.Client.PARAM_TYPE_ERROR,
                String.format("参数'%s'类型不匹配，期望类型：%s", e.getName(), e.getRequiredType().getSimpleName()));
        logException(exception, request);
        return buildResponseEntity(exception, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理认证服务异常
     */
    @ExceptionHandler(AuthenticationServiceException.class)
    public ResponseEntity<Result<Object>> handleAuthenticationServiceException(AuthenticationServiceException e, HttpServletRequest request) {
        logException(e, request);

        // 认证服务异常通常返回500，因为这是服务端问题
        return buildResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 处理其他未捕获异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Object>> handleException(Exception e, HttpServletRequest request) {
        log.error("未捕获异常: {} {}", request.getMethod(), request.getRequestURI(), e);
        SystemException exception = ExceptionFactory.system(ErrorCode.System.SYSTEM_ERROR, "系统异常，请稍后重试", e);
        return buildResponseEntity(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 构建统一响应实体 - 使用Result类
     */
    private ResponseEntity<Result<Object>> buildResponseEntity(BaseRuntimeException e, HttpStatus status) {
        Result<Object> result = Result.fail(e.getCode(), e.getMessage());
        return ResponseEntity.status(status).body(result);
    }

    /**
     * 记录异常日志
     */
    private void logException(BaseRuntimeException e, HttpServletRequest request) {
        String logTemplate = "{} {} - 错误码: {} - 请求ID: {}";
        Object[] args = new Object[]{
                request.getMethod(),
                request.getRequestURI(),
                e.getCode(),
                e.getTraceId()
        };

        if (e.getAlarmLevelEnum().getLevel() >= AlarmLevelEnum.HIGH.getLevel()) {
            log.error(logTemplate + " - 异常详情: {}", args, e.getMessage(), e);
        } else if (e.getAlarmLevelEnum().getLevel() >= AlarmLevelEnum.MEDIUM.getLevel()) {
            log.warn(logTemplate, args);
        } else {
            log.info(logTemplate, args);
        }
    }
}