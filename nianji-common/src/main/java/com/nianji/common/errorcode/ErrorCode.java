package com.nianji.common.errorcode;

/**
 * 错误码接口 格式：A-BB-CCC A: 错误级别 (1-系统级, 2-业务级, 3-客户端, 4-第三方) BB: 模块编号 (00-99) CCC: 具体错误编号 (000-999)
 *
 * @author zhangjinming
 */
public interface ErrorCode {

    String getCode();

    String getMessage();

    default String getFullCode() {
        return getCode();
    }

    /**
     * 成功码 (0xxxx) - 操作成功
     */
    enum Success implements ErrorCode {
        SUCCESS("00000", "成功"),
        OPERATION_SUCCESS("00001", "操作成功"),
        CREATED("00002", "创建成功"),
        UPDATED("00003", "更新成功"),
        DELETED("00004", "删除成功"),
        PROCESSED("00005", "处理成功");

        private final String code;
        private final String message;

        Success(String code, String message) {
            this.code = code;
            this.message = message;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    /**
     * 系统级错误 (1xxxx) - 系统基础设施问题
     */
    enum System implements ErrorCode {
        // 通用系统错误
        SYSTEM_ERROR("10001", "系统异常"),
        SYSTEM_BUSY("10002", "系统繁忙，请稍后重试"),
        SYSTEM_OVERLOAD("10003", "系统过载"),
        FEATURE_DISABLED("10004", "功能暂未开放"),
        CONFIG_ERROR("10005", "系统配置错误"),

        // 数据存储错误 (101xx)
        DATABASE_ERROR("10101", "数据库服务异常"),
        DATABASE_CONNECTION_ERROR("10102", "数据库连接异常"),
        DATABASE_TIMEOUT("10103", "数据库操作超时"),
        DATABASE_DEADLOCK("10104", "数据库死锁"),
        DUPLICATE_KEY_ERROR("10105", "数据唯一键冲突"),

        // 缓存错误 (102xx)
        CACHE_ERROR("10201", "缓存服务异常"),
        CACHE_CONNECTION_ERROR("10202", "缓存连接异常"),
        CACHE_TIMEOUT("10203", "缓存操作超时"),
        CACHE_SERIALIZATION_ERROR("10204", "缓存序列化异常"),

        // 消息队列错误 (103xx)
        MQ_ERROR("10301", "消息队列异常"),
        MQ_SEND_FAILED("10302", "消息发送失败"),
        MQ_CONSUME_FAILED("10303", "消息消费失败"),

        // 加解密错误 (104xx)
        CRYPTO_UNKNOWN("10400", "未知的加解密方式"),
        CRYPTO_ERROR("10401", "加解密服务异常"),
        ENCRYPT_FAILED("10402", "加密失败"),
        DECRYPT_FAILED("10403", "解密失败"),
        UNSUPPORTED_DECRYPT("10404", "不支持解密"),
        CRYPTO_GENERATE_FAILED("10405", "密钥生成失败"),
        SIGN_GENERATE_FAILED("10406", "签名生成失败"),
        SIGN_VERIFY_FAILED("10407", "签名验证失败"),
        KEY_GENERATION_FAILED("10408", "密钥生成失败"),

        // 文件操作错误 (105xx)
        FILE_OPERATION_ERROR("10501", "文件操作异常"),
        FILE_UPLOAD_FAILED("10502", "文件上传失败"),
        FILE_DOWNLOAD_FAILED("10503", "文件下载失败"),
        FILE_NOT_FOUND("10504", "文件不存在"),

        // 认证服务错误 (107xx)
        AUTH_SERVICE_ERROR("10701", "认证服务异常"),
        TOKEN_GENERATION_FAILED("10702", "令牌生成失败"),
        TOKEN_REFRESH_FAILED("10703", "令牌刷新失败"),
        TOKEN_STORAGE_FAILED("10704", "令牌存储失败"),
        TOKEN_REVOCATION_FAILED("10705", "令牌撤销失败"),
        TOKEN_SIGNATURE_ERROR("10706", "令牌签名异常"),
        TOKEN_PARSING_ERROR("10707", "令牌解析异常"),
        JWT_TOKEN_INVALID("10708", "JWT令牌格式无效"),

        ;

        private final String code;
        private final String message;

        System(String code, String message) {
            this.code = code;
            this.message = message;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    /**
     * 业务级错误 (2xxxx) - 业务逻辑问题
     */
    enum Business implements ErrorCode {

        /**
         * 通用业务错误
         */
        BUSINESS_ERROR("20001", "业务处理异常"),
        BUSINESS_RULE_VIOLATION("20002", "业务规则违反"),
        BUSINESS_VALIDATION_FAILED("20003", "业务校验失败"),

        // 数据相关错误 (201xx)
        DATA_NOT_FOUND("20101", "数据不存在"),
        DATA_ALREADY_EXISTS("20102", "数据已存在"),
        DATA_VERSION_CONFLICT("20103", "数据版本冲突"),
        DATA_STATE_INVALID("20104", "数据状态异常"),
        DATA_INTEGRITY_VIOLATION("20105", "数据完整性违反"),

        // 资源操作错误 (202xx)
        RESOURCE_NOT_FOUND("20201", "资源不存在"),
        RESOURCE_ALREADY_EXISTS("20202", "资源已存在"),
        RESOURCE_STATE_INVALID("20203", "资源状态异常"),
        RESOURCE_OPERATION_NOT_ALLOWED("20204", "资源操作不被允许"),
        RESOURCE_LIMIT_EXCEEDED("20205", "资源数量超限"),

        // 工作流错误 (203xx)
        WORKFLOW_ERROR("20301", "工作流异常"),
        APPROVAL_REQUIRED("20302", "需要审批"),
        APPROVAL_REJECTED("20303", "审批被拒绝"),
        WORKFLOW_STATE_INVALID("20304", "工作流状态异常"),

        // 用户相关错误 (204xx)
        USER_NOT_FOUND("20401", "用户不存在"),
        USER_DISABLED("20402", "用户已被禁用"),
        USER_CREDENTIALS_INVALID("20403", "用户凭证无效"),

        // 日记相关错误 (205xx)
        DIARY_NOT_FOUND("20501", "订单不存在"),
        DIARY_STATE_INVALID("20502", "订单状态异常"),
        DIARY_AMOUNT_INVALID("20503", "订单金额异常"),

        // 支付相关错误 (206xx)
        PAYMENT_FAILED("20601", "支付失败"),
        PAYMENT_AMOUNT_MISMATCH("20602", "支付金额不匹配"),
        PAYMENT_TIMEOUT("20603", "支付超时");

        private final String code;
        private final String message;

        Business(String code, String message) {
            this.code = code;
            this.message = message;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    /**
     * 客户端错误 (3xxxx) - 请求参数和权限问题
     */
    enum Client implements ErrorCode {
        // 通用客户端错误
        CLIENT_ERROR("30001", "客户端请求异常"),

        // 参数校验错误 (301xx)
        PARAM_NULL("30101", "请求参数为空"),
        PARAM_ERROR("30102", "请求参数错误"),
        PARAM_MISSING("30103", "缺少必要参数"),
        PARAM_FORMAT_ERROR("30104", "参数格式错误"),
        PARAM_TYPE_ERROR("30105", "参数类型错误"),
        PARAM_RANGE_ERROR("30106", "参数值超出范围"),
        PARAM_LENGTH_ERROR("30107", "参数长度不符合要求"),

        // 请求格式错误 (302xx)
        REQUEST_METHOD_NOT_SUPPORTED("30201", "不支持的请求方法"),
        MEDIA_TYPE_NOT_SUPPORTED("30202", "不支持的媒体类型"),
        REQUEST_BODY_MISSING("30203", "请求体不能为空"),
        REQUEST_BODY_TOO_LARGE("30204", "请求体过大"),

        // 认证错误 (303xx)
        UNAUTHORIZED("30301", "未授权访问"),
        AUTH_FAILED("30302", "认证失败"),
        TOKEN_MISSING("30303", "访问令牌缺失"),
        TOKEN_INVALID("30304", "访问令牌无效"),
        TOKEN_EXPIRED("30305", "访问令牌已过期"),
        REQUEST_EXPIRED("30306", "请求已过期"),
        INVALID_CREDENTIALS("30307", "用户名或密码错误"),
        ACCOUNT_LOCKED("30308", "账户已被锁定"),
        IP_LOCKED("30309", "IP已被锁定"),
        ACCOUNT_DISABLED("30310", "账户已被禁用"),
        SESSION_EXPIRED("30311", "会话已过期"),
        SESSION_LIMIT_EXCEEDED("30312", "会话数达到上限，请先退出其他设备"),
        DEVICE_VERIFICATION_REQUIRED("30313", "需要设备验证"),
        REMOTE_LOGIN_NOT_ALLOWED("30314", "不允许异地登录"),


        // 权限错误 (304xx)
        FORBIDDEN("30401", "权限不足"),
        ACCESS_DENIED("30402", "访问被拒绝"),
        OPERATION_NOT_ALLOWED("30403", "操作不被允许"),
        RESOURCE_ACCESS_DENIED("30404", "资源访问权限不足"),

        // 频率限制 (305xx) - 限流相关
        RATE_LIMIT_EXCEEDED("30501", "请求频率超限"),
        CONCURRENT_REQUEST_LIMIT("30502", "并发请求超限"),
        IP_RATE_LIMIT("30503", "IP请求频率超限"),
        USER_RATE_LIMIT("30504", "用户请求频率超限"),
        API_RATE_LIMIT("30505", "接口请求频率超限"),

        // 重复请求 (306xx)
        DUPLICATE_REQUEST("30601", "重复请求"),
        REQUEST_IDEMPOTENT_CONFLICT("30602", "幂等性冲突"),

        ;

        private final String code;
        private final String message;

        Client(String code, String message) {
            this.code = code;
            this.message = message;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    /**
     * 第三方服务错误 (4xxxx) - 外部依赖问题
     */
    enum ThirdParty implements ErrorCode {
        // 通用第三方错误
        THIRD_PARTY_ERROR("40001", "第三方服务异常"),
        THIRD_PARTY_UNAVAILABLE("40002", "第三方服务不可用"),
        THIRD_PARTY_TIMEOUT("40003", "第三方服务调用超时"),
        THIRD_PARTY_RATE_LIMIT("40004", "第三方服务频率限制"),

        // 支付服务错误 (401xx)
        PAYMENT_SERVICE_ERROR("40101", "支付服务异常"),
        PAYMENT_FAILED("40102", "支付失败"),
        PAYMENT_TIMEOUT("40103", "支付超时"),
        PAYMENT_AMOUNT_ERROR("40104", "支付金额错误"),

        // 短信服务错误 (402xx)
        SMS_SERVICE_ERROR("40201", "短信服务异常"),
        SMS_SEND_FAILED("40202", "短信发送失败"),
        SMS_TEMPLATE_ERROR("40203", "短信模板错误"),
        SMS_FREQUENCY_LIMIT("40204", "短信发送频率超限"),

        // 邮件服务错误 (403xx)
        EMAIL_SERVICE_ERROR("40301", "邮件服务异常"),
        EMAIL_SEND_FAILED("40302", "邮件发送失败"),
        EMAIL_TEMPLATE_ERROR("40303", "邮件模板错误"),

        // 对象存储错误 (404xx)
        OSS_ERROR("40401", "对象存储异常"),
        OSS_UPLOAD_FAILED("40402", "文件上传失败"),
        OSS_DOWNLOAD_FAILED("40403", "文件下载失败"),

        // AI服务错误 (405xx)
        AI_SERVICE_ERROR("40501", "AI服务异常"),
        AI_PROCESSING_FAILED("40502", "AI处理失败"),
        AI_SERVICE_TIMEOUT("40503", "AI服务响应超时");

        private final String code;
        private final String message;

        ThirdParty(String code, String message) {
            this.code = code;
            this.message = message;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }
}