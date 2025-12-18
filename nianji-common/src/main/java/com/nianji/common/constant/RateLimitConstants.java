package com.nianji.common.constant;

/**
 * 限流常量配置
 */
public final class RateLimitConstants {
    private RateLimitConstants() {
    }

    // ============ 限流类型枚举 ============
    public enum RateLimitType {
        // 认证模块
        LOGIN_IP,           // IP登录限流
        LOGIN_USER,         // 用户登录限流
        REGISTER_IP,        // IP注册限流
        REFRESH_TOKEN,      // 令牌刷新限流
        VERIFY_CODE_IP,     // IP验证码限流
        VERIFY_CODE_TARGET, // 目标验证码限流

        // Diary 日记模块
        DIARY_CREATE_IP,    // 创建日记IP限流
        DIARY_CREATE_USER,  // 创建日记用户限流
        DIARY_UPDATE_IP,    // 更新日记IP限流
        DIARY_UPDATE_USER,  // 更新日记用户限流
        DIARY_DELETE_IP,    // 删除日记IP限流
        DIARY_DELETE_USER,  // 删除日记用户限流
        DIARY_QUERY_IP,     // 查询日记IP限流
        DIARY_QUERY_USER,   // 查询日记用户限流
        DIARY_LIST_IP,      // 日记列表IP限流
        DIARY_LIST_USER,    // 日记列表用户限流

        // 通用API
        API_IP,             // API IP限流
        API_USER,           // API 用户限流
        GLOBAL              // 全局限流
    }

    // ============ 默认限流配置 ============
    /**
     * 默认限流时间窗口（秒）
     */
    public static final long DEFAULT_TIME_WINDOW = 60;
    /**
     * 默认最大请求次数
     */
    public static final long DEFAULT_MAX_REQUESTS = 100;

    // ============ 认证模块限流配置 ============
    /**
     * 登录接口 - 每个IP每分钟最多10次
     */
    public static final long LOGIN_IP_LIMIT = 10;
    public static final long LOGIN_IP_WINDOW = 60;

    /**
     * 登录接口 - 每个用户每分钟最多5次
     */
    public static final long LOGIN_USER_LIMIT = 5;
    public static final long LOGIN_USER_WINDOW = 60;

    /**
     * 注册接口 - 每个IP每小时最多5次
     */
    public static final long REGISTER_IP_LIMIT = 5;
    public static final long REGISTER_IP_WINDOW = 3600;

    /**
     * 刷新令牌 - 每个用户每小时最多20次
     */
    public static final long REFRESH_TOKEN_LIMIT = 20;
    public static final long REFRESH_TOKEN_WINDOW = 3600;

    /**
     * 验证码发送 - 每个IP每小时最多10次
     */
    public static final long VERIFY_CODE_IP_LIMIT = 10;
    public static final long VERIFY_CODE_IP_WINDOW = 3600;

    /**
     * 验证码发送 - 每个目标每小时最多3次
     */
    public static final long VERIFY_CODE_TARGET_LIMIT = 3;
    public static final long VERIFY_CODE_TARGET_WINDOW = 3600;

    // ============ Diary 模块限流配置 ============
    /**
     * 创建日记 - 每个IP每分钟最多10次
     */
    public static final long DIARY_CREATE_IP_LIMIT = 10;
    public static final long DIARY_CREATE_IP_WINDOW = 60;

    /**
     * 创建日记 - 每个用户每分钟最多5次
     */
    public static final long DIARY_CREATE_USER_LIMIT = 5;
    public static final long DIARY_CREATE_USER_WINDOW = 60;

    /**
     * 更新日记 - 每个IP每分钟最多20次
     */
    public static final long DIARY_UPDATE_IP_LIMIT = 20;
    public static final long DIARY_UPDATE_IP_WINDOW = 60;

    /**
     * 更新日记 - 每个用户每分钟最多10次
     */
    public static final long DIARY_UPDATE_USER_LIMIT = 10;
    public static final long DIARY_UPDATE_USER_WINDOW = 60;

    /**
     * 删除日记 - 每个IP每分钟最多5次
     */
    public static final long DIARY_DELETE_IP_LIMIT = 5;
    public static final long DIARY_DELETE_IP_WINDOW = 60;

    /**
     * 删除日记 - 每个用户每分钟最多3次
     */
    public static final long DIARY_DELETE_USER_LIMIT = 3;
    public static final long DIARY_DELETE_USER_WINDOW = 60;

    /**
     * 查询日记详情 - 每个IP每分钟最多50次
     */
    public static final long DIARY_QUERY_IP_LIMIT = 50;
    public static final long DIARY_QUERY_IP_WINDOW = 60;

    /**
     * 查询日记详情 - 每个用户每分钟最多30次
     */
    public static final long DIARY_QUERY_USER_LIMIT = 30;
    public static final long DIARY_QUERY_USER_WINDOW = 60;

    /**
     * 日记列表 - 每个IP每分钟最多30次
     */
    public static final long DIARY_LIST_IP_LIMIT = 30;
    public static final long DIARY_LIST_IP_WINDOW = 60;

    /**
     * 日记列表 - 每个用户每分钟最多20次
     */
    public static final long DIARY_LIST_USER_LIMIT = 20;
    public static final long DIARY_LIST_USER_WINDOW = 60;


    /**
     * 普通API - 每个IP每分钟最多100次
     */
    public static final long API_IP_LIMIT = 100;
    public static final long API_IP_WINDOW = 60;

    /**
     * 普通API - 每个用户每分钟最多50次
     */
    public static final long API_USER_LIMIT = 50;
    public static final long API_USER_WINDOW = 60;
}