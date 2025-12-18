package com.nianji.diary.dao.repository.impl;

import com.nianji.diary.dao.repository.DiaryTagRelationRepository;
import org.springframework.stereotype.Repository;

/**
 * TODO
 *
 * @author zhangjinming
 * @version 0.0.1
 */
@Repository
public class DiaryTagRelationRepositoryImpl implements DiaryTagRelationRepository {
    @Autowired
    private DiaryTagMapper diaryTagMapper;

    @Override
    public void deleteById(Long diaryTagId) {
        diaryTagMapper.deleteById(diaryTagId);
    }

    @Override
    public void deleteByDiaryId(Long diaryId) {
        LambdaQueryWrapper<DiaryTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiaryTag::getDiaryId, diaryId);
        diaryTagMapper.delete(wrapper);
    }
}
