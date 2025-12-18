package com.nianji.diary.dao.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nianji.diary.dao.mapper.DiaryMapper;
import com.nianji.diary.dao.mapper.DiaryTagMapper;
import com.nianji.diary.dao.repository.DiaryRepository;
import com.nianji.diary.entity.Diary;
import com.nianji.diary.entity.Tag;
import com.nianji.diary.vo.DiaryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author zhangjinming
 * @version 0.0.1
 */
@Repository
public class DiaryRepositoryImpl implements DiaryRepository {

    @Autowired
    private DiaryTagMapper diaryTagMapper;
    @Autowired
    private DiaryTagMapper diaryTagMapper;
    @Autowired
    private DiaryMapper diaryMapper;

    @Override
    public void insert(Diary diary) {
        diaryMapper.insert(diary);
    }

    @Override
    public Diary selectById(Long diaryId) {
        return diaryMapper.selectById(diaryId);
    }

    @Override
    public void updateById(Diary diary) {
        diaryMapper.updateById(diary);
    }

    @Override
    public void deletById(Long diaryId) {
        diaryMapper.deleteById(diaryId);
    }

    @Override
    public Page<DiaryVO> selectDiaryPage(Page<DiaryVO> page, Long userId, String keyword, List<Long> tagIds) {
        // 构建基础查询条件
        LambdaQueryWrapper<Diary> wrapper = Wrappers.lambdaQuery(Diary.class)
                .eq(Diary::getUserId, userId);

        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Diary::getTitle, keyword)
                    .or()
                    .like(Diary::getContent, keyword));
        }

        // 标签筛选
        if (tagIds != null && !tagIds.isEmpty()) {
            // 查询包含这些标签的日记ID
            LambdaQueryWrapper<DiaryTag> tagWrapper = Wrappers.lambdaQuery(DiaryTag.class)
                    .in(DiaryTag::getTagId, tagIds);
            List<DiaryTag> diaryTags = diaryTagMapper.selectList(tagWrapper);

            if (!diaryTags.isEmpty()) {
                List<Long> diaryIds = diaryTags.stream()
                        .map(DiaryTag::getDiaryId)
                        .distinct()
                        .collect(Collectors.toList());
                wrapper.in(Diary::getId, diaryIds);
            } else {
                // 如果没有匹配的标签，返回空结果
                wrapper.eq(Diary::getId, -1L);
            }
        }

        // 排序
        wrapper.orderByDesc(Diary::getCreateTime);

        // 执行分页查询
        Page<Diary> diaryPage = diaryMapper.selectList(page, wrapper);

        // 转换为 VO 对象
        return convertToDiaryVOPage(diaryPage);
    }

    @Override
    public List<Tag> selectTagsByDiaryId(Long diaryId) {
        // 查询日记标签关联
        LambdaQueryWrapper<DiaryTag> wrapper = Wrappers.lambdaQuery(DiaryTag.class)
                .eq(DiaryTag::getDiaryId, diaryId);
        List<DiaryTag> diaryTags = diaryTagMapper.selectList(wrapper);

        if (diaryTags.isEmpty()) {
            return List.of();
        }

        // 获取标签ID列表
        List<Long> tagIds = diaryTags.stream()
                .map(DiaryTag::getTagId)
                .collect(Collectors.toList());

        // 查询标签详情
        LambdaQueryWrapper<Tag> tagWrapper = Wrappers.lambdaQuery(Tag.class)
                .in(Tag::getId, tagIds);
        return diaryTagMapper.selectList(tagWrapper);
    }

    @Override
    public List<Diary> selectDiariesByTagId(Long tagId, Long userId) {
        // 查询包含该标签的日记ID
        LambdaQueryWrapper<DiaryTag> diaryTagWrapper = Wrappers.lambdaQuery(DiaryTag.class)
                .eq(DiaryTag::getTagId, tagId);
        List<DiaryTag> diaryTags = diaryTagMapper.selectList(diaryTagWrapper);

        if (diaryTags.isEmpty()) {
            return List.of();
        }

        // 获取日记ID列表
        List<Long> diaryIds = diaryTags.stream()
                .map(DiaryTag::getDiaryId)
                .collect(Collectors.toList());

        // 查询日记详情
        LambdaQueryWrapper<Diary> diaryWrapper = Wrappers.lambdaQuery(Diary.class)
                .in(Diary::getId, diaryIds)
                .eq(Diary::getUserId, userId)
                .orderByDesc(Diary::getCreateTime);

        return diaryMapper.selectList(diaryWrapper);
    }

    /**
     * 将 Diary Page 转换为 DiaryVO Page
     */
    private Page<DiaryVO> convertToDiaryVOPage(Page<Diary> diaryPage) {
        Page<DiaryVO> voPage = new Page<>();
        voPage.setCurrent(diaryPage.getCurrent());
        voPage.setSize(diaryPage.getSize());
        voPage.setTotal(diaryPage.getTotal());
        voPage.setPages(diaryPage.getPages());

        // 转换记录
        List<DiaryVO> voRecords = diaryPage.getRecords().stream()
                .map(diary -> {
                    DiaryVO vo = new DiaryVO();
                    // 这里使用 BeanUtils 或手动设置属性
                    vo.setId(diary.getId());
                    vo.setUserId(diary.getUserId());
                    vo.setTitle(diary.getTitle());
                    vo.setContent(diary.getContent());
                    vo.setWeather(diary.getWeather());
                    vo.setMood(diary.getMood());
                    vo.setLocation(diary.getLocation());
                    vo.setIsPublic(diary.getIsPublic() == 1);
                    vo.setCreateTime(diary.getCreateTime());
                    vo.setUpdateTime(diary.getUpdateTime());
                    return vo;
                })
                .collect(Collectors.toList());

        voPage.setRecords(voRecords);
        return voPage;
    }
}
