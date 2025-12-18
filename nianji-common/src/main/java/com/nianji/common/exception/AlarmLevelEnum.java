package com.nianji.common.exception;

import lombok.Getter;

/**
 * 告警等级枚举 企业级告警级别定义
 */
@Getter
public enum AlarmLevelEnum {

    CRITICAL("严重", 3, true, true),    // 需要立即处理，影响核心业务
    HIGH("高", 2, true, false),        // 需要尽快处理，影响部分业务
    MEDIUM("中", 1, false, false),     // 需要关注处理，影响较小
    LOW("低", 0, false, false);        // 仅记录，无需立即处理

    private final String description;
    private final int level;
    private final boolean needAlert;      // 是否需要发送告警
    private final boolean needImmediate;  // 是否需要立即处理

    AlarmLevelEnum(String description, int level, boolean needAlert, boolean needImmediate) {
        this.description = description;
        this.level = level;
        this.needAlert = needAlert;
        this.needImmediate = needImmediate;
    }

    /**
     * 根据错误码自动确定告警级别
     */
    public static AlarmLevelEnum fromErrorCode(String errorCode) {
        if (errorCode == null) {
            return MEDIUM;
        }

        char firstChar = errorCode.charAt(0);
        return switch (firstChar) {
            case '1' -> // 系统级错误
                    CRITICAL;
            case '4' -> // 第三方服务错误
                    HIGH;
            case '3' -> {
                if (errorCode.startsWith("303") || errorCode.startsWith("304")) {
                    yield MEDIUM;
                }
                yield LOW;
            }
            case '2' -> // 业务错误
                    MEDIUM;
            default -> MEDIUM;
        };
    }
}