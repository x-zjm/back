
package com.nianji.common.exception.client;

import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.AlarmLevelEnum;
import com.nianji.common.exception.BaseRuntimeException;
import lombok.Getter;

/**
 * 限流异常
 *
 * @author zhangjinming
 */
@Getter
public class RateLimitException extends BaseRuntimeException {
    private final long resetTime;
    private final String limitKey;
    private final int limitCount;
    private final int remaining;

    public RateLimitException(ErrorCode.Client errorCode, String limitKey, long resetTime, int limitCount, int remaining) {
        super(errorCode, AlarmLevelEnum.MEDIUM);
        this.resetTime = resetTime;
        this.limitKey = limitKey;
        this.limitCount = limitCount;
        this.remaining = remaining;
    }

    public RateLimitException(ErrorCode.Client errorCode, String limitKey, String message, long resetTime, int limitCount, int remaining) {
        super(errorCode, message, AlarmLevelEnum.MEDIUM);
        this.resetTime = resetTime;
        this.limitKey = limitKey;
        this.limitCount = limitCount;
        this.remaining = remaining;
    }

    /**
     * 获取剩余重置时间（秒）
     */
    public long getRemainingSeconds()
    {
        return Math.max(0, (resetTime - System.currentTimeMillis()) / 1000);
    }

    /**
     * 获取限流详细信息
     */
    public String getRateLimitInfo() {
        return String.format("限流键: %s, 限制次数: %d, 剩余次数: %d, 重置时间: %d秒后",
                limitKey, limitCount, remaining, getRemainingSeconds());
    }

    @Override
    public String getFullMessage() {
        return String.format("[%s] %s - %s", getCode(), getMessage(), getRateLimitInfo());
    }
}