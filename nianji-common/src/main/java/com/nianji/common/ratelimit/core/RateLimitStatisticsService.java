package com.nianji.common.ratelimit.core;

import com.nianji.common.constant.RateLimitConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;

/**
 * 限流统计服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RateLimitStatisticsService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String RATE_LIMIT_STATS_KEY = "nianji:rate_limit:stats";

    /**
     * 记录限流事件
     */
    public void recordRateLimitEvent(RateLimitConstants.RateLimitType type, String key, boolean allowed) {
        String statsKey = RATE_LIMIT_STATS_KEY + ":" + type.name().toLowerCase();

        try {
            LocalDate today = LocalDate.now();
            String dateKey = today.toString();

            // 记录总请求数
            String totalKey = statsKey + ":total:" + dateKey;
            redisTemplate.opsForValue().increment(totalKey);
            redisTemplate.expire(totalKey, Duration.ofDays(7));

            if (!allowed) {
                // 记录被限流的请求
                String blockedKey = statsKey + ":blocked:" + dateKey;
                redisTemplate.opsForValue().increment(blockedKey);
                redisTemplate.expire(blockedKey, Duration.ofDays(7));

                // 记录具体被限流的键
                String detailKey = statsKey + ":detail:" + dateKey + ":" + key;
                redisTemplate.opsForValue().increment(detailKey);
                redisTemplate.expire(detailKey, Duration.ofDays(1));
            }

        } catch (Exception e) {
            log.error("记录限流统计失败", e);
        }
    }

    /**
     * 获取限流统计
     */
    public RateLimitStats getRateLimitStats(RateLimitConstants.RateLimitType type, LocalDate date) {
        String statsKey = RATE_LIMIT_STATS_KEY + ":" + type.name().toLowerCase();
        String dateKey = date.toString();

        Long total = getLongValue(statsKey + ":total:" + dateKey);
        Long blocked = getLongValue(statsKey + ":blocked:" + dateKey);

        return new RateLimitStats(type, date, total != null ? total : 0,
                blocked != null ? blocked : 0);
    }

    private Long getLongValue(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.valueOf(value.toString()) : null;
    }

    @Data
    @AllArgsConstructor
    public static class RateLimitStats {
        private RateLimitConstants.RateLimitType type;
        private LocalDate date;
        private long totalRequests;
        private long blockedRequests;

        public double getBlockRate() {
            return totalRequests > 0 ? (double) blockedRequests / totalRequests : 0.0;
        }
    }
}