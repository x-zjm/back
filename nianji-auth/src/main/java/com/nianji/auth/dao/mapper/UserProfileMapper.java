package com.nianji.auth.dao.mapper;


import com.nianji.auth.entity.UserProfile;
import com.nianji.common.mybatis.CustomBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface UserProfileMapper extends CustomBaseMapper<UserProfile> {
    
    /**
     * 根据用户ID查询用户扩展信息
     */
    @Select("SELECT * FROM user_profiles WHERE user_id = #{userId} AND deleted = 0")
    UserProfile selectByUserId(@Param("userId") Long userId);
}