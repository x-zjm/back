package com.nianji.auth.dao.mapper;


import com.nianji.auth.entity.LoginLog;
import com.nianji.common.mybatis.CustomBaseMapper;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface LoginLogMapper extends CustomBaseMapper<LoginLog> {
}