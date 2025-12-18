package com.nianji.auth.dao.repository;

import com.nianji.auth.entity.User;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * TODO
 *
 * @author zhangjinming
 * @version 0.0.1
 */
public interface UserRepository {

    /**
     * 查找活跃用户并返回
     *
     * @param page
     *         页码
     * @param size
     *         数量
     * @return 活跃用户集合
     */
    List<User> findActiveUsers(long page, long size);

    /**
     * 查找活跃用户并汇总用户名、邮箱和手机号信息
     *
     * @param page
     *         页码
     * @param size
     *         数量
     * @return 活跃用户集合
     */
    List<String> findAllActiveIdentifiers(long page, long size);

    /**
     * 根据用户名查询用户是否存在
     *
     * @param username
     *         用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 根据邮箱查询用户是否存在
     *
     * @param email
     *         邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 根据手机号查询用户是否存在
     *
     * @param phone
     *         手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 根据对外唯一键查询用户是否存在
     *
     * @param publicId
     *         对外唯一键
     * @return 是否存在
     */
    boolean existsByPublicId(String publicId);

    /**
     * 插入用户
     *
     * @param user
     *         用户信息
     * @return 插入后生成的用户ID
     */
    int insert(User user);

    /**
     * 根据id查询用户
     *
     * @param userId
     *         主键索引
     * @return 用户信息
     */
    User selectById(Long userId);

    /**
     * 根据用户名查询用户
     *
     * @param username
     *         用户名
     * @return 用户信息
     */
    User selectByUsername(String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email
     *         邮箱
     * @return 用户信息
     */
    User selectByEmail(String email);

    /**
     * 根据手机号查询用户
     *
     * @param phone
     *         手机号
     * @return 用户信息
     */
    User selectByPhone(String phone);

    /**
     * 根据对外唯一键查询用户
     *
     * @param publicId
     *         对外唯一键
     * @return 返回用户信息
     */
    User selectByPublicId(String publicId);

    /**
     * 根据用户Id更新用户登录信息
     *
     * @param user
     *         用户信息
     * @return 受影响的行数
     */
    int updateById(User user);

    /**
     * 根据用户Id更新用户用户状态
     *
     * @param userId
     *         用户主键
     * @param status
     *         用户状态
     * @return 受影响的行数
     */
    int updateStatus(Long userId, Integer status);

}
