package com.nianji.common.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;

/**
 * @author zhangjinming
 * @description
 * @date 2025/3/1 16:38
 */
public interface CustomBaseMapper<T> extends BaseMapper<T> {

    int insertBatch(@Param(Constants.LIST) Collection<T> batchList);
}
