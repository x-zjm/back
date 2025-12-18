package com.nianji.auth.dao.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nianji.auth.entity.User;
import com.nianji.common.mybatis.CustomBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT id, username, email, phone, public_id FROM users WHERE status = 1 AND deleted = 0 ORDER BY id ASC")
    List<User> selectActiveUsers();
}