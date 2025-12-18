package com.nianji.auth.dao.repository.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nianji.auth.dao.mapper.UserMapper;
import com.nianji.auth.dao.repository.UserRepository;
import com.nianji.auth.entity.User;
import com.nianji.common.constant.CacheKeys;
import com.nianji.common.enums.DeletedEnum;
import com.nianji.common.enums.UserStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 *
 * @author zhangjinming
 * @version 0.0.1
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;

    @Override
    public List<User> findActiveUsers(long page, long size) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getStatus, UserStatusEnum.NORMAL.getCode()) // 状态正常
                .eq(User::getDeleted, DeletedEnum.NOT_DELETED.getCode()) // 未删除
                .select(User::getId, User::getUsername, User::getEmail, User::getPhone, User::getPublicId)
                .orderByAsc(User::getId);

        // 使用MyBatis Plus分页
        IPage<User> pageObj = new Page<>(page, size);

        IPage<User> resultPage = userMapper.selectPage(pageObj, queryWrapper);

        return ObjectUtil.isNotEmpty(resultPage) ?
                resultPage.getRecords()
                : new ArrayList<>();
    }

    @Override
    public List<String> findAllActiveIdentifiers(long page, long size) {
        List<User> activeUsers = findActiveUsers(page, size);

        // 提取所有标识符到一个列表中
        List<String> identifiers = new ArrayList<>();
        for (User user : activeUsers) {
            if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
                identifiers.add(user.getUsername());
            }
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                identifiers.add(user.getEmail());
            }
            if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
                identifiers.add(user.getPhone());
            }
        }

        return identifiers;
    }

    @Override
    public boolean existsByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, username)
                .eq(User::getDeleted, DeletedEnum.NOT_DELETED); // 未删除
        return userMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getEmail, email)
                .eq(User::getDeleted, DeletedEnum.NOT_DELETED);
        return userMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean existsByPhone(String phone) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getPhone, phone)
                .eq(User::getDeleted, DeletedEnum.NOT_DELETED);
        return userMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean existsByPublicId(String publicId) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getPublicId, publicId)
                .eq(User::getDeleted, DeletedEnum.NOT_DELETED);
        return userMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public int insert(User user) {
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        return userMapper.insert(user);
    }

    @Override
    @Cacheable(
            value = CacheKeys.Config.Names.USER_INFO,
            key = "T(com.nianji.common.constant.CacheKeys.User).infoById(#userId)",
            sync = true  // 同步加载，防止缓存击穿
    )
    public User selectById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    @Cacheable(
            value = CacheKeys.Config.Names.USER_INFO,
            key = "T(com.nianji.common.constant.CacheKeys.User).infoByUsername(#username)",
            sync = true  // 同步加载，防止缓存击穿
    )
    public User selectByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, username);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public User selectByEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getEmail, email)
                .eq(User::getDeleted, DeletedEnum.NOT_DELETED);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public User selectByPhone(String phone) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getPhone, phone)
                .eq(User::getDeleted, DeletedEnum.NOT_DELETED);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public User selectByPublicId(String publicId) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getPublicId, publicId)
                .eq(User::getDeleted, DeletedEnum.NOT_DELETED);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public int updateById(User user) {
        return userMapper.updateById(user);
    }

    @Override
    public int updateStatus(Long userId, Integer status) {
        LambdaUpdateWrapper<User> wrapper = Wrappers.lambdaUpdate(User.class)
                .eq(User::getId, userId);
        User user = User.builder()
                .status(status)
                .build();
        return userMapper.update(user, wrapper);
    }

}
