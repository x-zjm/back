package com.nianji.common.utils;

import com.nianji.common.config.CacheConfig;
import com.nianji.common.constant.CacheKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 统一缓存工具类 简化操作，支持对象和字符串缓存
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final CacheConfig cacheConfig;

    // ============ 对象缓存操作 ============

    /**
     * 获取缓存值
     */
    public <T> T get(String key) {
        try {
            return (T) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取缓存失败. key: {}", key, e);
            return null;
        }
    }

    /**
     * 设置缓存值
     */
    public void put(String key, Object value) {
        put(key, value, -1, TimeUnit.SECONDS);
    }

    /**
     * 设置缓存值（带过期时间）
     */
    public void put(String key, Object value, long timeout, TimeUnit unit) {
        try {
            if (timeout > 0) {
                redisTemplate.opsForValue().set(key, value, timeout, unit);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
        } catch (Exception e) {
            log.error("设置缓存失败. key: {}", key, e);
        }
    }

    /**
     * 智能设置缓存
     */
    public void putSmart(String key, Object value) {
        long expire = cacheConfig.getExpire(key);
        put(key, value, expire, TimeUnit.SECONDS);
    }

    /**
     * 添加值（带过期时间）
     */
    public void add(String key, Object value, long timeout, TimeUnit unit) {
        try {
            if (timeout > 0) {
                redisTemplate.opsForSet().add(key, value, timeout, unit);
            } else {
                redisTemplate.opsForSet().add(key, value);
            }
        } catch (Exception e) {
            log.error("添加值. key: {}", key, e);
        }
    }

    /**
     * 智能设置缓存（自动获取过期时间）
     */
    public void addSmart(String key, Object value) {
        long expire = cacheConfig.getExpire(key);
        add(key, value, expire, TimeUnit.SECONDS);
    }


    /**
     * 删除缓存
     */
    public boolean delete(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Exception e) {
            log.error("删除缓存失败. key: {}", key, e);
            return false;
        }
    }

    /**
     * 移除键值对
     */
    public void remove(String key, Object... objects) {
        try {
            redisTemplate.opsForSet().remove(key, objects);
        } catch (Exception e) {
            log.error("移除键值对. key: {}", key, e);
        }
    }

    /**
     * 汇总集合所有成员
     */
    public Set<Object> members(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("汇总集合所有成员. key: {}", key, e);
        }

        return null;
    }

    /**
     * 选取指定范围
     */
    public List<Object> range(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("选取指定范围. key: {}", key, e);
        }

        return null;
    }

    /**
     * 获取或计算缓存
     */
    public <T> T getOrCompute(String key, Supplier<T> supplier) {
        return getOrCompute(key, supplier, -1, TimeUnit.SECONDS);
    }

    /**
     * 获取或计算缓存（带过期时间）
     */
    public <T> T getOrCompute(String key, Supplier<T> supplier, long timeout, TimeUnit unit) {
        T value = get(key);
        if (value == null) {
            value = supplier.get();
            if (value != null) {
                put(key, value, timeout, unit);
            }
        }
        return value;
    }

    // ============ 字符串缓存操作 ============

    /**
     * 获取字符串缓存值
     */
    public String getString(String key) {
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取字符串缓存失败. key: {}", key, e);
            return null;
        }
    }

    /**
     * 设置字符串缓存值
     */
    public void putString(String key, String value) {
        putString(key, value, -1, TimeUnit.SECONDS);
    }

    /**
     * 设置字符串缓存值（带过期时间）
     */
    public void putString(String key, String value, long timeout, TimeUnit unit) {
        try {
            if (timeout > 0) {
                stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
            } else {
                stringRedisTemplate.opsForValue().set(key, value);
            }
        } catch (Exception e) {
            log.error("设置字符串缓存失败. key: {}", key, e);
        }
    }

    /**
     * 智能设置字符串缓存（自动获取过期时间）
     */
    public void putStringSmart(String key, String value) {
        long expire = cacheConfig.getExpire(key);
        putString(key, value, expire, TimeUnit.SECONDS);
    }

    /**
     * 删除缓存（StringRedisTemplate）
     */
    public boolean deleteString(String key) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
        } catch (Exception e) {
            log.error("删除缓存失败（StringRedisTemplate）. key: {}", key, e);
            return false;
        }
    }

    /**
     * 从左侧插入（RedisTemplate）
     */
    public Long leftPush(String key, Object value) {
        try {
            return redisTemplate.opsForList().leftPush(key, value);
        } catch (Exception e) {
            log.error("从左侧插入（RedisTemplate）. key: {}", key, e);
            return null;
        }
    }

    /**
     * 从左侧插入（StringRedisTemplate）
     */
    public Long leftPushString(String key, String value) {
        try {
            return stringRedisTemplate.opsForList().leftPush(key, value);
        } catch (Exception e) {
            log.error("从左侧插入（StringRedisTemplate）. key: {}", key, e);
            return null;
        }
    }

    /**
     * 修剪（RedisTemplate）
     */
    public void trim(String key, long start, long end) {
        try {
            redisTemplate.opsForList().trim(key, start, end);
        } catch (Exception e) {
            log.error("修剪（RedisTemplate）. key: {}", key, e);
        }
    }

    /**
     * 修剪（StringRedisTemplate）
     */
    public void trimString(String key, long start, long end) {
        try {
            stringRedisTemplate.opsForList().trim(key, start, end);
        } catch (Exception e) {
            log.error("修剪（StringRedisTemplate）. key: {}", key, e);
        }
    }

    // ============ 计数器操作 ============

    /**
     * 自增操作（RedisTemplate）
     */
    public Long increment(String key) {
        return increment(key, 1);
    }

    /**
     * 自增操作（指定增量）（RedisTemplate）
     */
    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("缓存自增失败（RedisTemplate）. key: {}, delta: {}", key, delta, e);
            return null;
        }
    }

    /**
     * 自增操作（StringRedisTemplate）
     */
    public Long incrementString(String key) {
        return increment(key, 1);
    }

    /**
     * 自增操作（指定增量）（StringRedisTemplate）
     */
    public Long incrementString(String key, long delta) {
        try {
            return stringRedisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("缓存自增失败（StringRedisTemplate）. key: {}, delta: {}", key, delta, e);
            return null;
        }
    }

    /**
     * 自减操作（RedisTemplate）
     */
    public Long decrement(String key) {
        return decrement(key, 1);
    }

    /**
     * 自减操作（指定减量）（RedisTemplate）
     */
    public Long decrement(String key, long delta) {
        try {
            return redisTemplate.opsForValue().decrement(key, delta);
        } catch (Exception e) {
            log.error("缓存自减失败（RedisTemplate）. key: {}, delta: {}", key, delta, e);
            return null;
        }
    }

    /**
     * 自减操作（StringRedisTemplate）
     */
    public Long decrementString(String key) {
        return decrement(key, 1);
    }

    /**
     * 自减操作（指定减量）（StringRedisTemplate）
     */
    public Long decrementString(String key, long delta) {
        try {
            return stringRedisTemplate.opsForValue().decrement(key, delta);
        } catch (Exception e) {
            log.error("缓存自减失败（StringRedisTemplate）. key: {}, delta: {}", key, delta, e);
            return null;
        }
    }

    /**
     * 自增并设置过期时间（RedisTemplate）
     */
    public Long incrementWithExpire(String key, long delta, long timeout, TimeUnit unit) {
        try {
            Long result = redisTemplate.opsForValue().increment(key, delta);
            if (timeout > 0) {
                stringRedisTemplate.expire(key, timeout, unit);
            }
            return result;
        } catch (Exception e) {
            log.error("缓存自增并设置过期时间失败.（RedisTemplate） key: {}, delta: {}", key, delta, e);
            return null;
        }
    }

    /**
     * 自增并设置过期时间（StringRedisTemplate）
     */
    public Long incrementWithExpireString(String key, long delta, long timeout, TimeUnit unit) {
        try {
            Long result = stringRedisTemplate.opsForValue().increment(key, delta);
            if (timeout > 0) {
                stringRedisTemplate.expire(key, timeout, unit);
            }
            return result;
        } catch (Exception e) {
            log.error("缓存自增并设置过期时间失败.（StringRedisTemplate） key: {}, delta: {}", key, delta, e);
            return null;
        }
    }

    // ============ 键和过期时间操作 ============

    /**
     * 检查键是否存在（RedisTemplate）
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("检查键是否存在失败(RedisTemplate). key: {}", key, e);
            return false;
        }
    }

    /**
     * 检查键是否存在（StringRedisTemplate）
     */
    public boolean hasKeyString(String key) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("检查键是否存在失败(StringRedisTemplate). key: {}", key, e);
            return false;
        }
    }

    /**
     * 设置过期时间（RedisTemplate）
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
        } catch (Exception e) {
            log.error("设置过期时间失败(RedisTemplate). key: {}", key, e);
            return false;
        }
    }

    /**
     * 设置过期时间（StringRedisTemplate）
     */
    public boolean expireString(String key, long timeout, TimeUnit unit) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.expire(key, timeout, unit));
        } catch (Exception e) {
            log.error("设置过期时间失败(StringRedisTemplate). key: {}", key, e);
            return false;
        }
    }

    /**
     * 获取剩余的过期时间（RedisTemplate）
     */
    public long getExpire(String key) {
        return getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 获取剩余的过期时间（指定时间单位）（RedisTemplate）
     */
    public long getExpire(String key, TimeUnit unit) {
        try {
            Long expire = redisTemplate.getExpire(key, unit);
            return expire != null ? expire : -2;
        } catch (Exception e) {
            log.error("获取过期时间失败（RedisTemplate）. key: {}", key, e);
            return -2;
        }
    }

    /**
     * 获取剩余的过期时间（StringRedisTemplate）
     */
    public long getExpireString(String key) {
        return getExpireString(key, TimeUnit.SECONDS);
    }

    /**
     * 获取剩余的过期时间（指定时间单位）（StringRedisTemplate）
     */
    public long getExpireString(String key, TimeUnit unit) {
        try {
            Long expire = stringRedisTemplate.getExpire(key, unit);
            return expire != null ? expire : -2;
        } catch (Exception e) {
            log.error("获取过期时间失败（StringRedisTemplate）. key: {}", key, e);
            return -2;
        }
    }

    /**
     * 移除键的过期时间，使其永久有效
     */
    public boolean persist(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.persist(key));
        } catch (Exception e) {
            log.error("移除键过期时间失败. key: {}", key, e);
            return false;
        }
    }

    /**
     * 移除键的过期时间，使其永久有效（StringRedisTemplate）
     */
    public boolean persistString(String key) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.persist(key));
        } catch (Exception e) {
            log.error("移除键过期时间失败（StringRedisTemplate）. key: {}", key, e);
            return false;
        }
    }
}