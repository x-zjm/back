package com.nianji.auth.filter;

import com.nianji.auth.entity.User;
import com.nianji.auth.filter.impl.UserBloomFilterServiceImpl;

/**
 * TODO
 *
 * @author zhangjinming
 * @version 0.0.1
 */
public interface UserBloomFilterService {

    /**
     * 布隆过滤器层面检测用户名是否存在
     *
     * @param username
     *         用户名
     * @return 是否存在
     */
    boolean mightUsernameExist(String username);

    /**
     * 布隆过滤器层面检测邮箱是否存在
     *
     * @param email
     *         邮箱
     * @return 是否存在
     */
    boolean mightEmailExist(String email);

    /**
     * 布隆过滤器层面检测手机号是否存在
     *
     * @param phone
     *         手机号
     * @return 是否存在
     */
    boolean mightPhoneExist(String phone);

    /**
     * 添加用户至布隆过滤器
     *
     * @param user
     *         用户信息
     */
    void addUserToBloomFilter(User user);

    /**
     * 异步预热布隆过滤器
     */
    void warmUpBloomFiltersAsync();

    /**
     * 检查布隆过滤器是否可用
     */
    boolean isBloomFilterAvailable();

    /**
     * 是否正在使用备用方案
     */
    boolean isUsingFallback();

    /**
     * 获取布隆过滤器统计信息
     */
    UserBloomFilterServiceImpl.BloomFilterStats getBloomFilterStats();

    /**
     * 重置布隆过滤器
     */
    void resetBloomFilters();
}
