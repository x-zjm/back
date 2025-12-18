package com.nianji.diary.dao.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nianji.diary.dao.mapper.DiaryTagMapper;
import com.nianji.diary.dao.repository.DiaryTagRepository;
import com.nianji.diary.entity.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * TODO
 *
 * @author zhangjinming
 * @version 0.0.1
 */
@Repository
public class DiaryTagRepositoryImpl implements DiaryTagRepository {

    @Autowired
    private DiaryTagMapper diaryTagMapper;

    @Override
    public Tag selectOneByNameAndUserId(String tagName, Long userId) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getName, tagName)
                .eq(Tag::getUserId, userId);
        return diaryTagMapper.selectOne(wrapper);
    }
}
