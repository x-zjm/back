package com.nianji.diary.service.impl;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nianji.diary.dto.request.DiaryCreateRequest;
import com.nianji.diary.entity.Diary;
import com.nianji.diary.dao.mapper.DiaryMapper;
import com.nianji.diary.service.DiaryService;
import com.nianji.diary.service.DiaryTagService;
import com.nianji.diary.service.FileService;
import com.nianji.diary.vo.DiaryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Service
public class DiaryServiceImpl implements DiaryService {

    private final FileService fileService;
    private final DiaryMapper diaryMapper;
    private final DiaryTagService tagService;

    public DiaryServiceImpl(FileService fileService, DiaryMapper diaryMapper, DiaryTagService tagService) {
        this.fileService = fileService;
        this.diaryMapper = diaryMapper;
        this.tagService = tagService;
    }

    @Override
    @Transactional
    public Long createDiary(DiaryCreateRequest diaryCreateRequest, Long userId) {
        return null;
    }

    @Override
    @Transactional
    public void updateDiary(Diary diary, List<String> tagNames, List<MultipartFile> files, Long userId) {
        Diary existingDiary = diaryMapper.selectById(diary.getId());
        if (existingDiary == null || !existingDiary.getUserId().equals(userId)) {
            throw new RuntimeException("日记不存在或无权修改");
        }

        // 只有日记所有者可以修改
        diary.setUserId(userId);
        diaryMapper.updateById(diary);

        // 处理标签更新
        if (tagNames != null) {
            tagService.updateDiaryTags(tagNames, diary.getId(), userId);
        }

        // 处理新文件上传
        // processFiles(files, diary.getId(), userId);

        log.info("日记更新成功, ID: {}, 用户ID: {}", diary.getId(), userId);
    }

    @Override
    @Transactional
    public void updateDiaryTags(Long diaryId, List<String> tagNames, Long userId) {
        Diary diary = diaryMapper.selectById(diaryId);
        if (diary == null || !diary.getUserId().equals(userId)) {
            throw new RuntimeException("日记不存在或无权修改标签");
        }

        // 更新标签
        tagService.updateDiaryTags(tagNames, diaryId, userId);

        log.info("更新日记标签成功, 日记ID: {}, 用户ID: {}, 标签数量: {}", diaryId, userId, tagNames.size());
    }

    @Override
    public void deleteDiary(Long diaryId, Long userId) {

    }

    @Override
    public DiaryVO getDiaryWithAttachments(Long diaryId, Long userId) {
        return null;
    }

    @Override
    public List<DiaryVO> getUserDiaries(Long userId) {
        return null;
    }

    @Override
    public List<DiaryVO> getDiariesByTag(Long userId, String tagName) {
        return null;
    }

    @Override
    public List<DiaryVO> searchDiariesByTagKeyword(Long userId, String keyword) {
        return null;
    }

    @Override
    public boolean saveBatch(Collection<Diary> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<Diary> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean updateBatchById(Collection<Diary> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdate(Diary entity) {
        return false;
    }

    @Override
    public Diary getOne(Wrapper<Diary> queryWrapper, boolean throwEx) {
        return null;
    }

    @Override
    public Optional<Diary> getOneOpt(Wrapper<Diary> queryWrapper, boolean throwEx) {
        return Optional.empty();
    }

    @Override
    public Map<String, Object> getMap(Wrapper<Diary> queryWrapper) {
        return null;
    }

    @Override
    public <V> V getObj(Wrapper<Diary> queryWrapper, Function<? super Object, V> mapper) {
        return null;
    }

    @Override
    public BaseMapper<Diary> getBaseMapper() {
        return null;
    }

    @Override
    public Class<Diary> getEntityClass() {
        return null;
    }
}