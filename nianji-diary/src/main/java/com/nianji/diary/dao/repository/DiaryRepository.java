package com.nianji.diary.dao.repository;

import com.nianji.diary.entity.Diary;

/**
 * TODO
 *
 * @author zhangjinming
 * @version 0.0.1
 */
public interface DiaryRepository {

    void insert(Diary diary);

    Diary selectById(Long diaryId);

    void updateById(Diary diary);

    void deletById(Long diaryId);

}
