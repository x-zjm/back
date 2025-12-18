package com.nianji.diary.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.nianji.diary.entity.DiaryTag;


import java.util.List;
import java.util.Set;


public interface DiaryTagService extends IService<DiaryTag> {

    List<DiaryTag> getUserTags(Long userId);

    List<DiaryTag> getDiaryTags(Long diaryId);

    List<DiaryTag> getPopularTags(Long userId, int limit);

    DiaryTag createOrUpdateTag(String tagName, Long userId);

    void processTagsForDiary(List<String> tagNames, Long diaryId, Long userId);

    void updateDiaryTags(List<String> tagNames, Long diaryId, Long userId);

    void deleteUnusedTags(Long userId);

    List<String> extractTagsFromString(String tagString);

    Set<String> getAllUserTagNames(Long userId);

    List<DiaryTag> searchTagsByKeyword(Long userId, String keyword);

    int getUserTagCount(Long userId);
}