package com.nianji.auth.filter.impl;


import com.nianji.auth.dao.repository.UserRepository;
import com.nianji.auth.entity.User;
import com.nianji.auth.filter.UserBloomFilterService;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * 修复版布隆过滤器服务实现 解决 Redis 命令执行问题
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserBloomFilterServiceImpl implements UserBloomFilterService {


    private final RedissonClient redissonClient;
    private final UserRepository userRepository;

    // 使用 StringRedisTemplate 用于简单操作
    private final StringRedisTemplate stringRedisTemplate;

    // 使用主 RedisTemplate
    private final RedisTemplate<String, Object> redisTemplate;


    // Redis布隆过滤器
    private RBloomFilter<String> usernameBloomFilter;
    private RBloomFilter<String> emailBloomFilter;
    private RBloomFilter<String> phoneBloomFilter;


    // 布隆过滤器键名
    private static final String USERNAME_FILTER = "bloom:filter:username";
    private static final String EMAIL_FILTER = "bloom:filter:email";
    private static final String PHONE_FILTER = "bloom:filter:phone";


    // 分级初始化参数
    private static final long[] INSERTION_OPTIONS = {1000L, 5000L, 10000L};
    private static final double[] PROBABILITY_OPTIONS = {0.20, 0.10, 0.05};


    // 备用方案使用 Redis Set
    private static final String USERNAME_SET = "bloom:fallback:username";
    private static final String EMAIL_SET = "bloom:fallback:email";
    private static final String PHONE_SET = "bloom:fallback:phone";

    // 运行模式
    private BloomFilterMode currentMode = BloomFilterMode.BLOOM_FILTER;

    @PostConstruct
    public void initBloomFilters() {

        try {
            // 删除所有旧的用户缓存（根据你的缓存key模式）
            Set<String> keys = redisTemplate.keys("*"); // 根据你的key模式调整
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("清理了 {} 个旧的缓存项", keys.size());
            }
        } catch (Exception e) {
            log.warn("清理旧缓存失败", e);
        }

        log.info("开始初始化用户布隆过滤器...");
        long startTime = System.currentTimeMillis();


        try {
            // 1. 检查 Redis 健康状况（使用修复后的方法）
            RedisHealthStatus healthStatus = checkRedisHealth();
            if (!healthStatus.isHealthy()) {
                log.warn("Redis 健康状况不佳，直接启用备用方案。内存使用率: {}%, 错误: {}",
                        healthStatus.getMemoryUsagePercent(), healthStatus.getErrorMessage());
                enableFallbackMode();
                warmUpFallbackFiltersAsync();
                return;
            }


            // 2. 分级初始化布隆过滤器
            boolean usernameInit = initializeBloomFilterWithRetry(USERNAME_FILTER, "用户名");
            boolean emailInit = initializeBloomFilterWithRetry(EMAIL_FILTER, "邮箱");
            boolean phoneInit = initializeBloomFilterWithRetry(PHONE_FILTER, "手机号");


            log.info("布隆过滤器初始化结果 - 用户名: {}, 邮箱: {}, 手机号: {}", usernameInit, emailInit, phoneInit);


            // 3. 根据初始化结果决定运行模式
            if (usernameInit && emailInit && phoneInit) {
                currentMode = BloomFilterMode.BLOOM_FILTER;
                log.info("布隆过滤器模式启用成功");
                warmUpBloomFiltersAsync();
            } else if (usernameInit || emailInit || phoneInit) {
                currentMode = BloomFilterMode.MIXED;
                log.warn("混合模式启用 - 部分布隆过滤器可用");
                warmUpBloomFiltersAsync();
            } else {
                enableFallbackMode();
                warmUpFallbackFiltersAsync();
            }


            long endTime = System.currentTimeMillis();
            log.info("布隆过滤器初始化完成，模式: {}, 耗时: {}ms", currentMode, (endTime - startTime));


        } catch (Exception e) {
            log.error("初始化布隆过滤器过程中发生异常，启用备用方案", e);
            enableFallbackMode();
            warmUpFallbackFiltersAsync();
        }
    }


    /**
     * 修复的 Redis 健康检查方法
     */
    private RedisHealthStatus checkRedisHealth() {
        RedisHealthStatus status = new RedisHealthStatus();

        try {
            // 1. 连接测试
            String pong = stringRedisTemplate.getConnectionFactory().getConnection().ping();
            if (!"PONG".equals(pong)) {
                status.setHealthy(false);
                status.setErrorMessage("Redis 连接测试失败");
                return status;
            }


            // 2. 获取内存信息（返回 Properties）
            Properties memoryInfo = stringRedisTemplate.execute(
                    (RedisCallback<Properties>) connection -> connection.serverCommands().info("memory")
            );

            if (memoryInfo == null) {
                status.setHealthy(false);
                status.setErrorMessage("无法获取 Redis 内存信息");
                return status;
            }


            // 3. 从 Properties 中解析内存信息
            long usedMemory = 0;
            long maxMemory = 0;

            // 获取 used_memory
            String usedMemoryStr = memoryInfo.getProperty("used_memory");
            if (usedMemoryStr != null) {
                usedMemory = Long.parseLong(usedMemoryStr);
            }

            // 获取 maxmemory
            String maxMemoryStr = memoryInfo.getProperty("maxmemory");
            if (maxMemoryStr != null && !"0".equals(maxMemoryStr)) {
                maxMemory = Long.parseLong(maxMemoryStr);
            }

            status.setUsedMemory(usedMemory);
            status.setMaxMemory(maxMemory);


            // 4. 内存使用率检查
            if (maxMemory > 0) {
                double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
                status.setMemoryUsagePercent(memoryUsagePercent);

                if (memoryUsagePercent > 90) {
                    status.setHealthy(false);
                    status.setErrorMessage("Redis 内存使用率超过90%: " + String.format("%.2f", memoryUsagePercent) + "%");
                    return status;
                }
            }


            status.setHealthy(true);
            return status;

        } catch (Exception e) {
            log.error("Redis 健康检查失败", e);
            status.setHealthy(false);
            status.setErrorMessage("健康检查异常: " + e.getMessage());
            return status;
        }
    }

    /**
     * 分级重试初始化布隆过滤器
     */
    private boolean initializeBloomFilterWithRetry(String filterName, String description) {
        // 先清理可能存在的损坏过滤器
        cleanupExistingFilter(filterName);


        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(filterName);

        // 分级重试
        for (int i = 0; i < INSERTION_OPTIONS.length; i++) {
            long insertions = INSERTION_OPTIONS[i];
            double probability = PROBABILITY_OPTIONS[i];

            try {
                log.info("尝试初始化 {} 布隆过滤器，参数: expectedInsertions={}, falseProbability={}",
                        description, insertions, probability);

                boolean initialized = bloomFilter.tryInit(insertions, probability);

                if (initialized) {
                    log.info("{} 布隆过滤器初始化成功，参数: {}/{}", description, insertions, probability);

                    // 保存引用
                    switch (filterName) {
                        case USERNAME_FILTER:
                            usernameBloomFilter = bloomFilter;
                            break;
                        case EMAIL_FILTER:
                            emailBloomFilter = bloomFilter;
                            break;
                        case PHONE_FILTER:
                            phoneBloomFilter = bloomFilter;
                            break;
                    }
                    return true;
                } else {
                    log.warn("{} 布隆过滤器初始化失败，参数: {}/{}", description, insertions, probability);
                    cleanupExistingFilter(filterName);
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                log.error("初始化 {} 布隆过滤器异常，参数: {}/{}: {}",
                        description, insertions, probability, e.getMessage());
                cleanupExistingFilter(filterName);
            }
        }

        log.error("{} 布隆过滤器所有重试均失败", description);
        return false;
    }


    /**
     * 清理已存在的过滤器
     */
    private void cleanupExistingFilter(String filterName) {
        try {
            redissonClient.getKeys().delete(filterName);
            Thread.sleep(50);
        } catch (Exception e) {
            log.debug("清理布隆过滤器 {} 失败: {}", filterName, e.getMessage());
        }
    }


    /**
     * 启用备用方案
     */
    private void enableFallbackMode() {
        this.currentMode = BloomFilterMode.FALLBACK_SET;
        log.warn("已启用备用方案模式: {}", currentMode);
    }


    // ============ 布隆过滤器检查方法 ============


    @Override
    public boolean mightUsernameExist(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        String normalizedUsername = username.trim().toLowerCase();

        try {
            switch (currentMode) {
                case BLOOM_FILTER:
                    return usernameBloomFilter != null && usernameBloomFilter.contains(normalizedUsername);

                case MIXED:
                    if (usernameBloomFilter != null) {
                        return usernameBloomFilter.contains(normalizedUsername);
                    }
                    // 降级到备用方案
                    return checkWithSet(normalizedUsername, USERNAME_SET);

                case FALLBACK_SET:
                default:
                    return checkWithSet(normalizedUsername, USERNAME_SET);
            }
        } catch (Exception e) {
            log.error("检查用户名存在性失败: {}", username, e);
            return true;
        }
    }


    @Override
    public boolean mightEmailExist(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String normalizedEmail = email.trim().toLowerCase();

        try {
            switch (currentMode) {
                case BLOOM_FILTER:
                    return emailBloomFilter != null && emailBloomFilter.contains(normalizedEmail);

                case MIXED:
                    if (emailBloomFilter != null) {
                        return emailBloomFilter.contains(normalizedEmail);
                    }
                    return checkWithSet(normalizedEmail, EMAIL_SET);

                case FALLBACK_SET:
                default:
                    return checkWithSet(normalizedEmail, EMAIL_SET);
            }
        } catch (Exception e) {
            log.error("检查邮箱存在性失败: {}", email, e);
            return true;
        }
    }


    @Override
    public boolean mightPhoneExist(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String normalizedPhone = phone.trim();

        try {
            switch (currentMode) {
                case BLOOM_FILTER:
                    return phoneBloomFilter != null && phoneBloomFilter.contains(normalizedPhone);

                case MIXED:
                    if (phoneBloomFilter != null) {
                        return phoneBloomFilter.contains(normalizedPhone);
                    }
                    return checkWithSet(normalizedPhone, PHONE_SET);

                case FALLBACK_SET:
                default:
                    return checkWithSet(normalizedPhone, PHONE_SET);
            }
        } catch (Exception e) {
            log.error("检查手机号存在性失败: {}", phone, e);
            return true;
        }
    }


    /**
     * 使用 Redis Set 检查存在性
     */
    private boolean checkWithSet(String value, String key) {
        try {
            Boolean exists = stringRedisTemplate.opsForSet().isMember(key, value);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Set 检查失败: {}", key, e);
            return true;
        }
    }


    // ============ 异步预热方法 ============


    /**
     * 异步预热布隆过滤器
     */
    @Override
    @Async("bloomFilterExecutor")
    public void warmUpBloomFiltersAsync() {
        if (currentMode == BloomFilterMode.FALLBACK_SET) {
            log.info("当前运行模式为 {}，跳过布隆过滤器预热", currentMode);
            return;
        }


        log.info("开始异步预热用户布隆过滤器，模式: {}...", currentMode);
        long startTime = System.currentTimeMillis();

        try {
            int page = 1;
            int size = 500;
            int totalUsers = 0;
            List<User> users;

            do {
                users = userRepository.findActiveUsers(page, size);
                for (User user : users) {
                    addUserToFilters(user);
                }

                totalUsers += users.size();
                page++;

                if (page % 10 == 0) {
                    log.debug("已预热 {} 页用户数据，共 {} 个用户", page, totalUsers);
                }
            } while (!users.isEmpty());


            long endTime = System.currentTimeMillis();
            log.info("用户布隆过滤器预热完成，共处理 {} 个用户，耗时 {} ms", totalUsers, (endTime - startTime));

        } catch (Exception e) {
            log.error("预热布隆过滤器失败", e);
        }
    }


    /**
     * 异步预热备用过滤器
     */
    @Async("bloomFilterExecutor")
    public void warmUpFallbackFiltersAsync() {
        log.info("开始异步预热备用过滤器，模式: {}...", currentMode);
        long startTime = System.currentTimeMillis();

        try {
            int page = 1;
            int size = 500;
            int totalUsers = 0;
            List<User> users;

            do {
                users = userRepository.findActiveUsers(page, size);
                for (User user : users) {
                    addUserToFallbackFilters(user);
                }

                totalUsers += users.size();
                page++;

                if (page % 10 == 0) {
                    log.debug("已预热 {} 页用户数据到备用过滤器，共 {} 个用户", page, totalUsers);
                }
            } while (!users.isEmpty());

            // 设置过期时间
            stringRedisTemplate.expire(USERNAME_SET, 30, TimeUnit.DAYS);
            stringRedisTemplate.expire(EMAIL_SET, 30, TimeUnit.DAYS);
            stringRedisTemplate.expire(PHONE_SET, 30, TimeUnit.DAYS);


            long endTime = System.currentTimeMillis();
            log.info("备用过滤器预热完成，共处理 {} 个用户，耗时 {} ms", totalUsers, (endTime - startTime));

        } catch (Exception e) {
            log.error("预热备用过滤器失败", e);
        }
    }


    // ============ 添加用户到过滤器 ============


    @Override
    public void addUserToBloomFilter(User user) {
        if (user == null) return;

        try {
            addUserToFilters(user);
            log.debug("用户 {} 的标识符已添加到过滤器", user.getUsername());
        } catch (Exception e) {
            log.error("添加用户到过滤器失败: {}", user.getUsername(), e);
        }
    }


    /**
     * 根据当前模式添加用户到相应过滤器
     */
    private void addUserToFilters(User user) {
        switch (currentMode) {
            case BLOOM_FILTER:
                if (usernameBloomFilter != null && user.getUsername() != null) {
                    usernameBloomFilter.add(user.getUsername().toLowerCase());
                }
                if (emailBloomFilter != null && user.getEmail() != null) {
                    emailBloomFilter.add(user.getEmail().toLowerCase());
                }
                if (phoneBloomFilter != null && user.getPhone() != null) {
                    phoneBloomFilter.add(user.getPhone());
                }
                break;

            case MIXED:
                if (usernameBloomFilter != null && user.getUsername() != null) {
                    usernameBloomFilter.add(user.getUsername().toLowerCase());
                } else {
                    addToSet(user.getUsername(), USERNAME_SET);
                }

                if (emailBloomFilter != null && user.getEmail() != null) {
                    emailBloomFilter.add(user.getEmail().toLowerCase());
                } else {
                    addToSet(user.getEmail(), EMAIL_SET);
                }

                if (phoneBloomFilter != null && user.getPhone() != null) {
                    phoneBloomFilter.add(user.getPhone());
                } else {
                    addToSet(user.getPhone(), PHONE_SET);
                }
                break;

            case FALLBACK_SET:
                addUserToFallbackFilters(user);
                break;
        }
    }


    /**
     * 添加用户到备用过滤器
     */
    private void addUserToFallbackFilters(User user) {
        addToSet(user.getUsername(), USERNAME_SET);
        addToSet(user.getEmail(), EMAIL_SET);
        addToSet(user.getPhone(), PHONE_SET);
    }


    private void addToSet(String value, String key) {
        if (value == null || value.trim().isEmpty()) return;
        try {
            stringRedisTemplate.opsForSet().add(key, value.trim().toLowerCase());
        } catch (Exception e) {
            log.error("添加到 Set 失败: {}", key, e);
        }
    }


    // ============ 状态检查和管理方法 ============


    @Override
    public boolean isBloomFilterAvailable() {
        return currentMode == BloomFilterMode.BLOOM_FILTER || currentMode == BloomFilterMode.MIXED;
    }


    @Override
    public boolean isUsingFallback() {
        return currentMode == BloomFilterMode.FALLBACK_SET;
    }


    @Override
    public BloomFilterStats getBloomFilterStats() {
        BloomFilterStats stats = new BloomFilterStats();
        stats.setCurrentMode(currentMode);

        try {
            switch (currentMode) {
                case BLOOM_FILTER:
                    if (usernameBloomFilter != null) stats.setUsernameCount(usernameBloomFilter.count());
                    if (emailBloomFilter != null) stats.setEmailCount(emailBloomFilter.count());
                    if (phoneBloomFilter != null) stats.setPhoneCount(phoneBloomFilter.count());
                    break;

                case MIXED:
                    if (usernameBloomFilter != null) {
                        stats.setUsernameCount(usernameBloomFilter.count());
                    } else {
                        Long count = stringRedisTemplate.opsForSet().size(USERNAME_SET);
                        stats.setUsernameCount(count != null ? count : 0);
                    }
                    // 类似处理 email 和 phone
                    break;

                case FALLBACK_SET:
                    Long usernameSize = stringRedisTemplate.opsForSet().size(USERNAME_SET);
                    Long emailSize = stringRedisTemplate.opsForSet().size(EMAIL_SET);
                    Long phoneSize = stringRedisTemplate.opsForSet().size(PHONE_SET);
                    stats.setUsernameCount(usernameSize != null ? usernameSize : 0);
                    stats.setEmailCount(emailSize != null ? emailSize : 0);
                    stats.setPhoneCount(phoneSize != null ? phoneSize : 0);
                    break;
            }
        } catch (Exception e) {
            log.error("获取布隆过滤器统计信息失败", e);
        }

        return stats;
    }


    // ============ 重置方法 ============


    @Override
    public void resetBloomFilters() {
        log.info("重置布隆过滤器...");

        try {
            // 删除布隆过滤器
            if (usernameBloomFilter != null) redissonClient.getKeys().delete(USERNAME_FILTER);
            if (emailBloomFilter != null) redissonClient.getKeys().delete(EMAIL_FILTER);
            if (phoneBloomFilter != null) redissonClient.getKeys().delete(PHONE_FILTER);

            // 删除备用过滤器
            stringRedisTemplate.delete(USERNAME_SET);
            stringRedisTemplate.delete(EMAIL_SET);
            stringRedisTemplate.delete(PHONE_SET);

            // 重置状态
            usernameBloomFilter = null;
            emailBloomFilter = null;
            phoneBloomFilter = null;
            currentMode = BloomFilterMode.BLOOM_FILTER;

            // 重新初始化
            initBloomFilters();

            log.info("布隆过滤器重置完成");
        } catch (Exception e) {
            log.error("重置布隆过滤器失败", e);
        }
    }


    // ============ 内部类和枚举 ============


    /**
     * 运行模式枚举
     */
    public enum BloomFilterMode {
        BLOOM_FILTER,    // 纯布隆过滤器模式
        MIXED,           // 混合模式（部分布隆过滤器+部分备用）
        FALLBACK_SET     // Redis Set 备用方案
    }


    /**
     * Redis 健康状态
     */
    @Data
    public static class RedisHealthStatus {
        private boolean healthy;
        private double memoryUsagePercent;
        private long usedMemory;
        private long maxMemory;
        private String errorMessage;
    }


    /**
     * 布隆过滤器统计信息
     */
    @Data
    public static class BloomFilterStats {
        private BloomFilterMode currentMode;
        private long usernameCount;
        private long emailCount;
        private long phoneCount;
        private boolean usingFallback = false;
    }
}