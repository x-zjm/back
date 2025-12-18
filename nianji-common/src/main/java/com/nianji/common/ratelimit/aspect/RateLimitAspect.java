package com.nianji.common.ratelimit.aspect;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.nianji.common.exception.ExceptionFactory;
import com.nianji.common.exception.client.RateLimitException;
import com.nianji.common.ratelimit.api.RateLimitService;
import com.nianji.common.ratelimit.annotation.RateLimit;
import com.nianji.common.ratelimit.annotation.RateLimits;
import com.nianji.common.config.RateLimitProperties;
import com.nianji.common.constant.RateLimitConstants;
import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.reqres.RequestModel;
import com.nianji.common.utils.IpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * 限流切面 - 集成新异常体系版本
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties rateLimitProperties;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(rateLimit)")
    public Object aroundSingleRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        return processRateLimit(joinPoint, new RateLimit[]{rateLimit});
    }

    @Around("@annotation(rateLimits)")
    public Object aroundMultipleRateLimits(ProceedingJoinPoint joinPoint, RateLimits rateLimits) throws Throwable {
        return processRateLimit(joinPoint, rateLimits.value());
    }

    private Object processRateLimit(ProceedingJoinPoint joinPoint, RateLimit[] rateLimits) throws Throwable {
        if (!rateLimitProperties.isEnabled()) {
            return joinPoint.proceed();
        }

        for (RateLimit rateLimit : rateLimits) {
            checkRateLimit(joinPoint, rateLimit);
        }

        return joinPoint.proceed();
    }

    private void checkRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        try {
            RateLimitConstants.RateLimitType type = rateLimit.type();
            RateLimitProperties.RateLimitConfig config = rateLimitProperties.getConfig(type);

            if (config == null) {
                log.warn("未找到限流配置: {}", type);
                return;
            }

            long limit = rateLimit.limit() > 0 ? rateLimit.limit() : config.getLimit();
            long window = rateLimit.window() > 0 ? rateLimit.window() : config.getWindow();

            String rateLimitKey = buildRateLimitKey(joinPoint, rateLimit, type);

            if (!rateLimitService.isAllowed(rateLimitKey, limit, window)) {
                long remaining = rateLimitService.getRemainingRequests(rateLimitKey, limit, window);
                long resetTime = rateLimitService.getResetTime(rateLimitKey, window);

                String errorMessage = rateLimit.message();
                log.warn("请求被限流 - 类型: {}, Key: {}, 限制: {}/{}, 剩余: {}, 重置时间: {}秒",
                        type, rateLimitKey, limit, window, remaining, resetTime);

                // 使用新的异常工厂创建限流异常
                throw ExceptionFactory.rateLimit(
                        ErrorCode.Client.RATE_LIMIT_EXCEEDED,
                        rateLimitKey,
                        errorMessage,
                        resetTime,
                        (int) limit,
                        (int) remaining
                );
            }
        } catch (RateLimitException e) {
            throw e; // 直接重新抛出限流异常
        } catch (Exception e) {
            log.error("限流检查异常: {}", e.getMessage(), e);
            // 其他异常转换为系统异常
            throw ExceptionFactory.system(
                    ErrorCode.System.SYSTEM_ERROR,
                    "限流服务异常",
                    e
            );
        }
    }

    private String buildRateLimitKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit, RateLimitConstants.RateLimitType type) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(type.name().toLowerCase()).append(":");

        if (StrUtil.isNotBlank(rateLimit.key())) {
            String spelKey = evaluateSpEL(rateLimit.key(), joinPoint);
            keyBuilder.append(spelKey);
        } else {
            keyBuilder.append(buildDefaultKey(joinPoint, type));
        }

        return keyBuilder.toString();
    }

    private String buildDefaultKey(ProceedingJoinPoint joinPoint, RateLimitConstants.RateLimitType type) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();

        HttpServletRequest request = getCurrentRequest();
        if (ObjectUtil.isNull(request)) {
            return methodName;
        }

        return switch (type) {
            case LOGIN_IP, REGISTER_IP, VERIFY_CODE_IP, DIARY_CREATE_IP, DIARY_UPDATE_IP,
                 DIARY_DELETE_IP, DIARY_QUERY_IP, DIARY_LIST_IP, API_IP -> {
                String ip = IpUtil.getIpAddr(request);
                yield ip + ":" + methodName;
            }
            case LOGIN_USER, REFRESH_TOKEN, DIARY_CREATE_USER, DIARY_UPDATE_USER,
                 DIARY_DELETE_USER, DIARY_QUERY_USER, DIARY_LIST_USER, API_USER -> {
                String userId = extractUserIdFromRequest(request);
                yield (StrUtil.isNotBlank(userId) ? userId : "anonymous") + ":" + methodName;
            }
            case VERIFY_CODE_TARGET -> {
                String target = extractTargetFromRequest(request);
                yield (target != null ? target : "unknown") + ":" + methodName;
            }
            default -> methodName;
        };
    }

    /**
     * 评估SpEL表达式
     */
    private String evaluateSpEL(String expression, ProceedingJoinPoint joinPoint) {
        try {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            Object[] args = joinPoint.getArgs();
            String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);

            if (parameterNames == null) {
                log.warn("无法获取方法参数名");
                return "parameter_names_unavailable";
            }

            StandardEvaluationContext context = new StandardEvaluationContext();

            // 1. 设置所有原始参数
            for (int i = 0; i < parameterNames.length; i++) {
                if (i < args.length && parameterNames[i] != null) {
                    context.setVariable(parameterNames[i], args[i]);
                }
            }

            // 2. 处理RequestModel参数
            processRequestModelParameters(context, args);

            Expression expr = expressionParser.parseExpression(expression);
            Object value = expr.getValue(context);

            return value != null ? value.toString() : "null";

        } catch (Exception e) {
            log.warn("SpEL表达式解析失败: {}, 错误: {}", expression, e.getMessage());
            return "spel_error";
        }
    }

    /**
     * 处理RequestModel参数
     */
    private void processRequestModelParameters(StandardEvaluationContext context, Object[] args) {
        if (args == null) {
            return;
        }

        Set<String> usedAliases = new HashSet<>();
        int requestModelCount = 0;

        for (Object arg : args) {
            if (arg instanceof RequestModel<?> requestModel) {
                requestModelCount++;
                Object requestData = requestModel.getRequestData();

                if (requestData != null) {
                    // 检查是否为基本数据类型
                    if (isBasicType(requestData)) {
                        // 基本数据类型统一使用 requestData 作为别名
                        String alias = "requestData";

                        // 检查是否已经设置过 requestData 别名
                        if (usedAliases.contains(alias)) {
                            log.warn("检测到多个RequestModel包含基本数据类型，都使用'requestData'作为别名，可能导致SpEL表达式解析错误");
                        } else {
                            context.setVariable(alias, requestData);
                            usedAliases.add(alias);
                            log.debug("设置基本数据类型别名: {}", alias);
                        }
                    } else {
                        // 对象类型使用类名首字母小写作为别名
                        String className = requestData.getClass().getSimpleName();
                        String alias = className.substring(0, 1).toLowerCase() + className.substring(1);

                        // 检查是否已经使用过该别名
                        if (usedAliases.contains(alias)) {
                            log.warn("检测到重复的RequestModel别名: {}，可能导致SpEL表达式解析错误", alias);
                        }

                        context.setVariable(alias, requestData);
                        usedAliases.add(alias);
                        log.debug("设置对象类型别名: {}", alias);
                    }
                }
            }
        }

        // 如果有多个RequestModel，记录警告但不阻断
        if (requestModelCount > 1) {
            log.warn("方法包含 {} 个RequestModel参数，SpEL表达式中可能存在别名冲突", requestModelCount);
        }
    }

    /**
     * 判断是否为基本数据类型
     */
    private boolean isBasicType(Object obj) {
        if (obj == null) {
            return false;
        }

        Class<?> clazz = obj.getClass();
        return clazz.isPrimitive() ||
                clazz.equals(String.class) ||
                Number.class.isAssignableFrom(clazz) ||
                clazz.equals(Boolean.class) ||
                clazz.equals(Character.class);
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.debug("获取当前请求失败: {}", e.getMessage());
            return null;
        }
    }

    private String extractUserIdFromRequest(HttpServletRequest request) {
        if (request == null) {
            return "anonymous";
        }

        try {
            String userId = request.getHeader("X-User-Id");
            if (StrUtil.isNotBlank(userId)) {
                return userId;
            }

            userId = request.getParameter("userId");
            if (StrUtil.isNotBlank(userId)) {
                return userId;
            }

            return "user_" + (request.getSession(false) != null ? request.getSession().getId() : "anonymous");
        } catch (Exception e) {
            log.debug("提取用户ID失败: {}", e.getMessage());
            return "anonymous";
        }
    }

    private String extractTargetFromRequest(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        try {
            String email = request.getParameter("email");
            if (StrUtil.isNotBlank(email)) {
                return email;
            }

            String phone = request.getParameter("phone");
            if (StrUtil.isNotBlank(phone)) {
                return phone;
            }

            return "unknown";
        } catch (Exception e) {
            log.debug("提取目标失败: {}", e.getMessage());
            return "unknown";
        }
    }
}