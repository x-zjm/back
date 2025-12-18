package com.nianji.common.exception;

import com.nianji.common.context.CustomRequestContext;
import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.business.*;
import com.nianji.common.exception.client.*;
import com.nianji.common.exception.system.*;
import com.nianji.common.reqres.BizResult;
import com.nianji.common.reqres.Result;

/**
 * 异常工厂类 - 基于Result响应类优化
 *
 * @author zhangjinming
 */
public final class ExceptionFactory {

    private ExceptionFactory() {
    }

    // ========== 客户端异常 ==========

    public static ValidationException validation(ErrorCode.Client code) {
        return new ValidationException(code);
    }

    public static ValidationException validation(ErrorCode.Client code, String message) {
        return new ValidationException(code, message);
    }

    public static ValidationException validation(ErrorCode.Client code, String message, Throwable cause) {
        return new ValidationException(code, message, cause);
    }

    public static AuthenticationException authentication(ErrorCode.Client code) {
        return new AuthenticationException(code);
    }

    public static AuthenticationException authentication(ErrorCode.Client code, String message) {
        return new AuthenticationException(code, message);
    }

    public static AuthorizationException authorization(ErrorCode.Client code) {
        return new AuthorizationException(code);
    }

    public static AuthorizationException authorization(ErrorCode.Client code, String message) {
        return new AuthorizationException(code, message);
    }

    public static RateLimitException rateLimit(ErrorCode.Client code, String limitKey, long resetTime, int limitCount, int remaining) {
        return new RateLimitException(code, limitKey, resetTime, limitCount, remaining);
    }

    public static RateLimitException rateLimit(ErrorCode.Client code, String limitKey, String message, long resetTime, int limitCount, int remaining) {
        return new RateLimitException(code, limitKey, message, resetTime, limitCount, remaining);
    }

    // ========== 业务异常 ==========

    public static BusinessException business(ErrorCode.Business code) {
        return new BusinessException(code);
    }

    public static BusinessException business(ErrorCode.Business code, String message) {
        return new BusinessException(code, message);
    }

    public static BusinessException business(ErrorCode.Business code, String message, Throwable cause) {
        return new BusinessException(code, message, cause);
    }

    public static BusinessException business(String code, String message) {
        return new BusinessException(code, message);
    }

    public static DataNotFoundException notFound(ErrorCode.Business code) {
        return new DataNotFoundException(code);
    }

    public static DataNotFoundException notFound(ErrorCode.Business code, String message) {
        return new DataNotFoundException(code, message);
    }

    public static DataNotFoundException notFound(ErrorCode.Business code, String message, Throwable cause) {
        return new DataNotFoundException(code, message, cause);
    }

    public static DuplicateException duplicate(ErrorCode.Business code) {
        return new DuplicateException(code);
    }

    public static DuplicateException duplicate(ErrorCode.Business code, String message) {
        return new DuplicateException(code, message);
    }

    public static WorkflowException workflow(ErrorCode.Business code) {
        return new WorkflowException(code);
    }

    public static WorkflowException workflow(ErrorCode.Business code, String message) {
        return new WorkflowException(code, message);
    }

    // ========== 系统异常 ==========

    public static SystemException system(ErrorCode.System code) {
        return new SystemException(code);
    }

    public static SystemException system(ErrorCode.System code, String message) {
        return new SystemException(code, message);
    }

    public static SystemException system(ErrorCode.System code, Throwable cause) {
        return new SystemException(code, cause);
    }

    public static SystemException system(ErrorCode.System code, String message, Throwable cause) {
        return new SystemException(code, message, cause);
    }

    public static DatabaseException database(ErrorCode.System code) {
        return new DatabaseException(code);
    }

    public static DatabaseException database(ErrorCode.System code, String message) {
        return new DatabaseException(code, message);
    }

    public static DatabaseException database(ErrorCode.System code, String message, Throwable cause) {
        return new DatabaseException(code, message, cause);
    }

    public static CacheException cache(ErrorCode.System code) {
        return new CacheException(code);
    }

    public static CacheException cache(ErrorCode.System code, String message) {
        return new CacheException(code, message);
    }

    public static CacheException cache(ErrorCode.System code, String message, Throwable cause) {
        return new CacheException(code, message, cause);
    }

    public static CryptoException crypto(ErrorCode.System code) {
        return new CryptoException(code);
    }

    public static CryptoException crypto(ErrorCode.System code, String message) {
        return new CryptoException(code, message);
    }

    public static CryptoException crypto(ErrorCode.System code, String message, Throwable cause) {
        return new CryptoException(code, message, cause);
    }

    // ========== 认证服务异常 ==========

    public static AuthenticationServiceException authService(ErrorCode.System code) {
        return new AuthenticationServiceException(code);
    }

    public static AuthenticationServiceException authService(ErrorCode.System code, String message) {
        return new AuthenticationServiceException(code, message);
    }

    public static AuthenticationServiceException authService(ErrorCode.System code, String tokenType, String operation) {
        return new AuthenticationServiceException(code, tokenType, operation);
    }

    public static AuthenticationServiceException authService(ErrorCode.System code, String message, String tokenType, String operation) {
        return new AuthenticationServiceException(code, message, tokenType, operation);
    }

    public static AuthenticationServiceException authService(ErrorCode.System code, String message, Throwable cause) {
        return new AuthenticationServiceException(code, message, cause);
    }

    public static AuthenticationServiceException authService(ErrorCode.System code, String message, String tokenType, String operation, Throwable cause) {
        return new AuthenticationServiceException(code, message, tokenType, operation, cause);
    }

    public static AuthenticationServiceException tokenGenerationFailed(String tokenType) {
        return new AuthenticationServiceException(
                ErrorCode.System.TOKEN_GENERATION_FAILED,
                tokenType,
                "generate"
        );
    }

    public static AuthenticationServiceException tokenGenerationFailed(String tokenType, String message) {
        return new AuthenticationServiceException(
                ErrorCode.System.TOKEN_GENERATION_FAILED,
                message,
                tokenType,
                "generate"
        );
    }

    public static AuthenticationServiceException tokenRefreshFailed(String tokenType) {
        return new AuthenticationServiceException(
                ErrorCode.System.TOKEN_REFRESH_FAILED,
                tokenType,
                "refresh"
        );
    }

    public static AuthenticationServiceException tokenSignatureError(String tokenType) {
        return new AuthenticationServiceException(
                ErrorCode.System.TOKEN_SIGNATURE_ERROR,
                tokenType,
                "sign"
        );
    }

    // ========== 第三方异常 ==========

    public static ExternalServiceException external(ErrorCode.ThirdParty code, String serviceName) {
        return new ExternalServiceException(code, serviceName);
    }

    public static ExternalServiceException external(ErrorCode.ThirdParty code, String serviceName, String message) {
        return new ExternalServiceException(code, serviceName, message);
    }

    public static ExternalServiceException external(ErrorCode.ThirdParty code, String serviceName, String serviceUrl, String message) {
        return new ExternalServiceException(code, serviceName, serviceUrl, message);
    }

    public static ExternalServiceException external(ErrorCode.ThirdParty code, String serviceName, Throwable cause) {
        return new ExternalServiceException(code, serviceName, cause);
    }

    public static ExternalServiceException external(ErrorCode.ThirdParty code, String serviceName, String message, Throwable cause) {
        return new ExternalServiceException(code, serviceName, message, cause);
    }

    public static ExternalServiceException external(ErrorCode.ThirdParty code, String serviceName, String serviceUrl, String message, Throwable cause) {
        return new ExternalServiceException(code, serviceName, serviceUrl, message, cause);
    }

    // ========== Result相关便捷方法 ==========

    /**
     * 创建成功的Result结果
     */
    public static <T> Result<T> successResult(T data) {
        return Result.success(data);
    }

    /**
     * 创建成功的Result结果（带自定义消息）
     */
    public static <T> Result<T> successResult(T data, String message) {
        return Result.success(data, message);
    }

    /**
     * 创建失败的Result结果
     */
    public static <T> Result<T> failResult(ErrorCode errorCode) {
        return Result.fail(errorCode);
    }

    /**
     * 创建失败的Result结果（带自定义消息）
     */
    public static <T> Result<T> failResult(ErrorCode errorCode, String message) {
        return Result.fail(errorCode, message);
    }

    /**
     * 创建失败的Result结果（带数据）
     */
    public static <T> Result<T> failResult(String errorCode, String message, T data) {
        return Result.fail(errorCode, message, data);
    }

    /**
     * 从异常创建BizResult
     */
    public static <T> BizResult<T> bizResultFromException(BaseRuntimeException e) {
        return BizResult.fail(e);
    }

    /**
     * 从异常创建Result
     */
    public static <T> Result<T> resultFromException(BaseRuntimeException e) {
        return Result.fail(e);
    }

    /**
     * 创建带监控信息的成功Result
     */
    public static <T> Result<T> successResultWithMetrics(T data) {
        long costTime = CustomRequestContext.calculateRequestDuration();
        return Result.success(data).withCostTime(costTime);
    }
}