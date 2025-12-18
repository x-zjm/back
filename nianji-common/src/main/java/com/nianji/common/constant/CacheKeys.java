package com.nianji.common.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 企业级缓存键常量 - 统一配置版本
 */
public final class CacheKeys {

    private CacheKeys() {
    }

    // ============ 基础配置 ============
    public static final String PROJECT_PREFIX = "nianji";
    private static final String SEPARATOR = ":";

    // ============ 统一的过期时间配置（秒） ============
    public static final class Expire {
        // 基础时间单位
        public static final long SHORT = 300L;          // 5分钟
        public static final long MEDIUM = 1800L;        // 30分钟
        public static final long LONG = 3600L;          // 1小时
        public static final long DAY = 86400L;          // 1天
        public static final long WEEK = 604800L;        // 7天
        public static final long MONTH = 2592000L;      // 30天

        // 业务特定过期时间
        public static final long ACCESS_TOKEN = 2 * LONG;        // 2小时
        public static final long REFRESH_TOKEN = WEEK;
        public static final long LOGIN_ATTEMPTS = MEDIUM;
        public static final long ACCOUNT_LOCK = 30 * LONG;       // 30分钟
        public static final long IP_LOCK = LONG;
        public static final long CAPTCHA = SHORT;
        public static final long PASSWORD_RESET = 900L;          // 15分钟
        public static final long USER_INFO = DAY;
        public static final long USER_PERMISSIONS = DAY;
        public static final long SESSION_INFO = WEEK;
        public static final long DEVICE_INFO = MONTH;
        public static final long BLACKLIST = WEEK;
        public static final long CONFIG = DAY;

        private Expire() {
        }
    }

    // ============ 统一配置管理器 ============
    public static final class Config {

        // Spring Cache 名称定义
        public static final class Names {
            public static final String USER_INFO = "user:info";
            public static final String USER_AUTH = "user:auth";
            public static final String USER_PROFILE = "user:profile";
            public static final String AUTH_ACCESS_TOKEN = "auth:access:token";
            public static final String AUTH_REFRESH_TOKEN = "auth:refresh:token";
            public static final String AUTH_TOKEN_MAPPING = "auth:token:mapping";
            public static final String AUTH_SESSION = "auth:session";
            public static final String AUTH_SECURITY = "auth:security";
            public static final String SYSTEM_CONFIG = "system:config";
            public static final String SECURITY_BLACKLIST = "security:blacklist";
            public static final String SSO_SESSION = "sso:session";
            public static final String SSO_CLIENT = "sso:client";
            public static final String SSO_CODE = "sso:code";
            public static final String CAPTCHA_IMAGE = "captcha:image";
            public static final String CAPTCHA_SMS = "captcha:sms";
            public static final String CAPTCHA_EMAIL = "captcha:email";
            public static final String RESET_PASSWORD = "reset:password";
            public static final String RESET_VERIFY = "reset:verify";

            private Names() {
            }
        }

        // 统一的配置映射（Spring Cache 名称 + 键模式 → 过期时间）
        private static final Map<String, Long> CONFIG_MAPPINGS = new HashMap<>();

        static {
            // ============ 用户模块 ============
            // Spring Cache 配置
            CONFIG_MAPPINGS.put(Names.USER_INFO, Expire.USER_INFO);
            CONFIG_MAPPINGS.put(Names.USER_AUTH, Expire.USER_PERMISSIONS);
            CONFIG_MAPPINGS.put(Names.USER_PROFILE, Expire.MEDIUM);

            // 键模式配置
            CONFIG_MAPPINGS.put("user:user:info:", Expire.USER_INFO);
            CONFIG_MAPPINGS.put("user:permission:", Expire.USER_PERMISSIONS);
            CONFIG_MAPPINGS.put("user:role:", Expire.USER_PERMISSIONS);
            CONFIG_MAPPINGS.put("user:profile:", Expire.MEDIUM);
            CONFIG_MAPPINGS.put("user:user:exists:", Expire.LOGIN_ATTEMPTS);

            // ============ 认证模块 ============
            // Spring Cache 配置
            CONFIG_MAPPINGS.put(Names.AUTH_ACCESS_TOKEN, Expire.ACCESS_TOKEN);
            CONFIG_MAPPINGS.put(Names.AUTH_REFRESH_TOKEN, Expire.REFRESH_TOKEN);
            CONFIG_MAPPINGS.put(Names.AUTH_TOKEN_MAPPING, Expire.ACCESS_TOKEN);
            CONFIG_MAPPINGS.put(Names.AUTH_SESSION, Expire.SESSION_INFO);
            CONFIG_MAPPINGS.put(Names.AUTH_SECURITY, Expire.LOGIN_ATTEMPTS);

            // 键模式配置
            CONFIG_MAPPINGS.put("auth:token:access:", Expire.ACCESS_TOKEN);
            CONFIG_MAPPINGS.put("auth:token:refresh:", Expire.REFRESH_TOKEN);
            CONFIG_MAPPINGS.put("auth:token:mapping:", Expire.ACCESS_TOKEN);
            CONFIG_MAPPINGS.put("auth:session:info:", Expire.SESSION_INFO);
            CONFIG_MAPPINGS.put("auth:session:list:", Expire.SESSION_INFO);
            CONFIG_MAPPINGS.put("auth:session:active:", Expire.SESSION_INFO);
            CONFIG_MAPPINGS.put("auth:attempt:user:", Expire.LOGIN_ATTEMPTS);
            CONFIG_MAPPINGS.put("auth:attempt:ip:", Expire.LOGIN_ATTEMPTS);
            CONFIG_MAPPINGS.put("auth:lock:user:", Expire.ACCOUNT_LOCK);
            CONFIG_MAPPINGS.put("auth:lock:ip:", Expire.IP_LOCK);
            CONFIG_MAPPINGS.put("auth:device:trusted:", Expire.DEVICE_INFO);

            // ============ 会话模块 ============
            CONFIG_MAPPINGS.put("session:session:user:", Expire.SESSION_INFO);
            CONFIG_MAPPINGS.put("session:device:list:", Expire.DEVICE_INFO);
            CONFIG_MAPPINGS.put("session:session:active:", Expire.SESSION_INFO);
            CONFIG_MAPPINGS.put("session:session:info:", Expire.SESSION_INFO);

            // ============ 安全模块 ============
            // Spring Cache 配置
            CONFIG_MAPPINGS.put(Names.SECURITY_BLACKLIST, Expire.BLACKLIST);

            // 键模式配置
            CONFIG_MAPPINGS.put("security:blacklist:token:", Expire.BLACKLIST);
            CONFIG_MAPPINGS.put("security:blacklist:ip:", Expire.BLACKLIST);
            CONFIG_MAPPINGS.put("security:blacklist:user:", Expire.BLACKLIST);
            CONFIG_MAPPINGS.put("security:ratelimit:", Expire.SHORT);

            // ============ 系统模块 ============
            // Spring Cache 配置
            CONFIG_MAPPINGS.put(Names.SYSTEM_CONFIG, Expire.CONFIG);

            // 键模式配置
            CONFIG_MAPPINGS.put("system:config:", Expire.CONFIG);
            CONFIG_MAPPINGS.put("system:dict:", Expire.DAY);

            // ============ SSO模块 ============
            // Spring Cache 配置
            CONFIG_MAPPINGS.put(Names.SSO_SESSION, Expire.MEDIUM);
            CONFIG_MAPPINGS.put(Names.SSO_CLIENT, Expire.DAY);
            CONFIG_MAPPINGS.put(Names.SSO_CODE, Expire.SHORT);

            // 键模式配置
            CONFIG_MAPPINGS.put("sso:session:global:", Expire.MEDIUM);
            CONFIG_MAPPINGS.put("sso:client:app:", Expire.DAY);
            CONFIG_MAPPINGS.put("sso:auth:code:", Expire.SHORT);

            // ============ 验证码模块 ============
            // Spring Cache 配置
            CONFIG_MAPPINGS.put(Names.CAPTCHA_IMAGE, Expire.CAPTCHA);
            CONFIG_MAPPINGS.put(Names.CAPTCHA_SMS, Expire.CAPTCHA);
            CONFIG_MAPPINGS.put(Names.CAPTCHA_EMAIL, Expire.CAPTCHA);

            // 键模式配置
            CONFIG_MAPPINGS.put("captcha:image:", Expire.CAPTCHA);
            CONFIG_MAPPINGS.put("captcha:sms:", Expire.CAPTCHA);
            CONFIG_MAPPINGS.put("captcha:email:", Expire.CAPTCHA);

            // ============ 密码重置模块 ============
            // Spring Cache 配置
            CONFIG_MAPPINGS.put(Names.RESET_PASSWORD, Expire.PASSWORD_RESET);
            CONFIG_MAPPINGS.put(Names.RESET_VERIFY, Expire.CAPTCHA);

            // 键模式配置
            CONFIG_MAPPINGS.put("reset:password:token:", Expire.PASSWORD_RESET);
            CONFIG_MAPPINGS.put("reset:verify:email:", Expire.CAPTCHA);
            CONFIG_MAPPINGS.put("reset:verify:phone:", Expire.CAPTCHA);

            // ============ 其他模块 ============
            CONFIG_MAPPINGS.put("lock:", Expire.SHORT);
            CONFIG_MAPPINGS.put("stats:", Expire.DAY);
        }

        /**
         * 获取 Spring Cache 配置映射
         */
        public static Map<String, Long> getSpringCacheConfigs() {
            Map<String, Long> springConfigs = new HashMap<>();

            // 只返回 Spring Cache 名称的配置
            for (String cacheName : new String[]{
                Names.USER_INFO, Names.USER_AUTH, Names.USER_PROFILE,
                Names.AUTH_ACCESS_TOKEN, Names.AUTH_REFRESH_TOKEN, Names.AUTH_TOKEN_MAPPING,
                Names.AUTH_SESSION, Names.AUTH_SECURITY, Names.SYSTEM_CONFIG,
                Names.SECURITY_BLACKLIST, Names.SSO_SESSION, Names.SSO_CLIENT,
                Names.SSO_CODE, Names.CAPTCHA_IMAGE, Names.CAPTCHA_SMS,
                Names.CAPTCHA_EMAIL, Names.RESET_PASSWORD, Names.RESET_VERIFY
            }) {
                springConfigs.put(cacheName, CONFIG_MAPPINGS.get(cacheName));
            }

            return springConfigs;
        }

        /**
         * 根据缓存键推断过期时间
         */
        public static long inferExpire(String cacheKey) {
            // 移除项目前缀
            String keyWithoutPrefix = cacheKey.replace(PROJECT_PREFIX + ":", "");

            // 按模式匹配
            for (Map.Entry<String, Long> entry : CONFIG_MAPPINGS.entrySet()) {
                String pattern = entry.getKey();
                // 跳过 Spring Cache 名称（不含冒号的）
                if (!pattern.contains(":")) continue;

                if (keyWithoutPrefix.startsWith(pattern)) {
                    return entry.getValue();
                }
            }

            // 默认过期时间
            return Expire.MEDIUM;
        }

        /**
         * 根据 Spring Cache 名称获取过期时间
         */
        public static long getExpireForCacheName(String cacheName) {
            return CONFIG_MAPPINGS.getOrDefault(cacheName, Expire.MEDIUM);
        }

        /**
         * 检查配置是否存在
         */
        public static boolean contains(String key) {
            return CONFIG_MAPPINGS.containsKey(key);
        }
    }

    // ============ 键构建器 ============
    private static String build(String... parts) {
        return String.join(SEPARATOR, parts);
    }

    // ============ 用户模块缓存键 ============
    public static final class User {
        public static String infoById(Long userId) {
            return build(PROJECT_PREFIX, "user", "user", "info", userId.toString());
        }

        public static String infoByUsername(String username) {
            return build(PROJECT_PREFIX, "user", "user", "info", "username:" + username.toLowerCase());
        }

        public static String infoByEmail(String email) {
            return build(PROJECT_PREFIX, "user", "user", "info", "email:" + email.toLowerCase());
        }

        public static String infoByPhone(String phone) {
            return build(PROJECT_PREFIX, "user", "user", "info", "phone:" + phone);
        }

        public static String permissions(Long userId) {
            return build(PROJECT_PREFIX, "user", "permission", userId.toString());
        }

        public static String roles(Long userId) {
            return build(PROJECT_PREFIX, "user", "role", userId.toString());
        }

        public static String usernameExists(String username) {
            return build(PROJECT_PREFIX, "user", "user", "exists", "username:" + username.toLowerCase());
        }

        public static String emailExists(String email) {
            return build(PROJECT_PREFIX, "user", "user", "exists", "email:" + email.toLowerCase());
        }

        public static String phoneExists(String phone) {
            return build(PROJECT_PREFIX, "user", "user", "exists", "phone:" + phone);
        }

        public static String profile(Long userId) {
            return build(PROJECT_PREFIX, "user", "profile", userId.toString());
        }

        private User() {
        }
    }

    // ============ 认证模块缓存键 ============
    public static final class Auth {
        public static String accessToken(Long userId) {
            return build(PROJECT_PREFIX, "auth", "token", "access", userId.toString());
        }

        public static String refreshToken(String refreshToken) {
            return build(PROJECT_PREFIX, "auth", "token", "refresh", refreshToken);
        }

        public static String tokenMapping(String accessToken) {
            return build(PROJECT_PREFIX, "auth", "token", "mapping", accessToken);
        }

        public static String userSessions(Long userId) {
            return build(PROJECT_PREFIX, "auth", "session", "list", userId.toString());
        }

        public static String sessionInfo(String sessionId) {
            return build(PROJECT_PREFIX, "auth", "session", "info", sessionId);
        }

        public static String activeSessions(Long userId) {
            return build(PROJECT_PREFIX, "auth", "session", "active", userId.toString());
        }

        public static String loginAttemptsByUser(String username) {
            return build(PROJECT_PREFIX, "auth", "attempt", "user", username.toLowerCase());
        }

        public static String loginAttemptsByIp(String ip) {
            return build(PROJECT_PREFIX, "auth", "attempt", "ip", ip);
        }

        public static String userLock(String username) {
            return build(PROJECT_PREFIX, "auth", "lock", "user", username.toLowerCase());
        }

        public static String ipLock(String ip) {
            return build(PROJECT_PREFIX, "auth", "lock", "ip", ip);
        }

        public static String trustedDevices(Long userId) {
            return build(PROJECT_PREFIX, "auth", "device", "trusted", userId.toString());
        }

        public static String successIp(Long userId) {
            return build(PROJECT_PREFIX, "auth", "success", "ip", userId.toString());
        }

        private Auth() {
        }
    }

    // ============ 会话模块缓存键 ============
    public static final class Session {
        public static String userSession(Long userId) {
            return build(PROJECT_PREFIX, "session", "session", "user", userId.toString());
        }

        public static String userDevices(Long userId) {
            return build(PROJECT_PREFIX, "session", "device", "list", userId.toString());
        }

        public static String activeSessionList(Long userId) {
            return build(PROJECT_PREFIX, "session", "session", "active", userId.toString());
        }

        public static String sessionDetail(String sessionId) {
            return build(PROJECT_PREFIX, "session", "session", "info", sessionId);
        }

        public static String lastActivity(Long userId) {
            return build(PROJECT_PREFIX, "session", "activity", "last", userId.toString());
        }

        private Session() {
        }
    }

    // ============ 安全模块缓存键 ============
    public static final class Security {
        public static String blacklistedToken(String token) {
            return build(PROJECT_PREFIX, "security", "blacklist", "token", token);
        }

        public static String blacklistedIp(String ip) {
            return build(PROJECT_PREFIX, "security", "blacklist", "ip", ip);
        }

        public static String blacklistedUser(Long userId) {
            return build(PROJECT_PREFIX, "security", "blacklist", "user", userId.toString());
        }

        public static String rateLimit(String type, String identifier) {
            return build(PROJECT_PREFIX, "security", "ratelimit", type, identifier);
        }

        public static String bloomFilterUsername() {
            return build(PROJECT_PREFIX, "security", "bloom", "username");
        }

        public static String bloomFilterEmail() {
            return build(PROJECT_PREFIX, "security", "bloom", "email");
        }

        public static String bloomFilterPhone() {
            return build(PROJECT_PREFIX, "security", "bloom", "phone");
        }

        public static String fallbackUsernameSet() {
            return build(PROJECT_PREFIX, "security", "fallback", "username");
        }

        public static String fallbackEmailSet() {
            return build(PROJECT_PREFIX, "security", "fallback", "email");
        }

        public static String fallbackPhoneSet() {
            return build(PROJECT_PREFIX, "security", "fallback", "phone");
        }

        // 预定义的限流类型常量
        public static final class RateLimitTypes {
            public static final String LOGIN_IP = "login_ip";
            public static final String LOGIN_USER = "login_user";
            public static final String REGISTER_IP = "register_ip";
            public static final String REFRESH_TOKEN = "refresh_token";
            public static final String API_IP = "api_ip";
            public static final String API_USER = "api_user";
            public static final String SMS = "sms";
            public static final String EMAIL = "email";
            public static final String DEFAULT = "default";

            private RateLimitTypes() {
            }
        }

        private Security() {
        }
    }

    // ============ 系统模块缓存键 ============
    public static final class System {
        public static String config(String configKey) {
            return build(PROJECT_PREFIX, "system", "config", configKey);
        }

        public static String dictionary(String dictType) {
            return build(PROJECT_PREFIX, "system", "dict", dictType);
        }

        private System() {
        }
    }

    // ============ SSO模块缓存键 ============
    public static final class Sso {
        public static String globalSession(String sessionId) {
            return build(PROJECT_PREFIX, "sso", "session", "global", sessionId);
        }

        public static String clientApp(String appId) {
            return build(PROJECT_PREFIX, "sso", "client", "app", appId);
        }

        public static String authorizationCode(String code) {
            return build(PROJECT_PREFIX, "sso", "auth", "code", code);
        }

        private Sso() {
        }
    }

    // ============ 验证码模块缓存键 ============
    public static final class Captcha {
        public static String image(String uuid) {
            return build(PROJECT_PREFIX, "captcha", "image", uuid);
        }

        public static String sms(String phone) {
            return build(PROJECT_PREFIX, "captcha", "sms", phone);
        }

        public static String email(String email) {
            return build(PROJECT_PREFIX, "captcha", "email", email.toLowerCase());
        }

        private Captcha() {
        }
    }

    // ============ 密码重置模块缓存键 ============
    public static final class Reset {
        public static String passwordToken(String token) {
            return build(PROJECT_PREFIX, "reset", "password", "token", token);
        }

        public static String verifyEmail(String email) {
            return build(PROJECT_PREFIX, "reset", "verify", "email", email.toLowerCase());
        }

        public static String verifyPhone(String phone) {
            return build(PROJECT_PREFIX, "reset", "verify", "phone", phone);
        }

        private Reset() {
        }
    }

    // ============ 分布式锁模块缓存键 ============
    public static final class Lock {
        public static String register(String username) {
            return build(PROJECT_PREFIX, "lock", "register", username.toLowerCase());
        }

        public static String login(String username) {
            return build(PROJECT_PREFIX, "lock", "login", username.toLowerCase());
        }

        public static String resetPassword(String email) {
            return build(PROJECT_PREFIX, "lock", "reset_password", email.toLowerCase());
        }

        public static String userOperation(Long userId, String operation) {
            return build(PROJECT_PREFIX, "lock", "user_operation", userId + ":" + operation);
        }

        private Lock() {
        }
    }

    // ============ 统计模块缓存键 ============
    public static final class Stats {
        public static String loginUser(Long userId, String date) {
            return build(PROJECT_PREFIX, "stats", "login", "user:" + userId + ":date:" + date);
        }

        public static String loginIp(String ip, String date) {
            return build(PROJECT_PREFIX, "stats", "login", "ip:" + ip + ":date:" + date);
        }

        public static String register(String date) {
            return build(PROJECT_PREFIX, "stats", "register", "date:" + date);
        }

        private Stats() {
        }
    }
}