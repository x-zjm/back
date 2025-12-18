package com.nianji.auth.service;

import com.nianji.auth.entity.User;

/**
 * 用户查询服务接口 负责用户信息的查询和验证，支持缓存优化
 */
public interface UserQueryService {

    /**
     * 根据用户ID获取用户信息（带缓存）
     *
     * @param userId
     *         用户ID
     * @return 用户信息，如果不存在返回null
     */
    User getUserById(Long userId);

    /**
     * 根据用户名获取用户信息（带缓存）
     *
     * @param username
     *         用户名
     * @return 用户信息，如果不存在返回null
     */
    User getUserByUsername(String username);

    /**
     * 检查用户名是否存在
     *
     * @param username
     *         用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     *
     * @param email
     *         邮箱地址
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     *
     * @param phone
     *         手机号码
     * @return 是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 根据邮箱获取用户信息
     *
     * @param email
     *         邮箱地址
     * @return 用户信息
     */
    User getUserByEmail(String email);

}
