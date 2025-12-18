package com.nianji.diary.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.nianji.diary.dto.request.DiaryCreateRequest;
import com.nianji.diary.entity.Diary;
import com.nianji.diary.vo.DiaryVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface DiaryService extends IService<Diary> {

    Long createDiary(DiaryCreateRequest diaryCreateRequest, Long userId);

    DiaryVO getDiaryWithAttachments(Long diaryId, Long userId);

    List<DiaryVO> getUserDiaries(Long userId);

    List<DiaryVO> getDiariesByTag(Long userId, String tagName);

    List<DiaryVO> searchDiariesByTagKeyword(Long userId, String keyword);

    void updateDiary(Diary diary, List<String> tagNames, List<MultipartFile> files, Long userId);

    void updateDiaryTags(Long diaryId, List<String> tagNames, Long userId);

    void deleteDiary(Long diaryId, Long userId);
}