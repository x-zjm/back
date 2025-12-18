package com.nianji.diary.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nianji.diary.entity.DiaryTag;
import com.nianji.diary.entity.DiaryTagRelation;
import com.nianji.diary.dao.mapper.DiaryTagMapper;
import com.nianji.diary.dao.mapper.DiaryTagRelationMapper;
import com.nianji.diary.service.DiaryTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class DiaryTagServiceImpl extends ServiceImpl<DiaryTagMapper, DiaryTag> implements DiaryTagService {


    private final DiaryTagMapper tagMapper;
    private final DiaryTagRelationMapper tagRelationMapper;

    public DiaryTagServiceImpl(DiaryTagMapper tagMapper, DiaryTagRelationMapper tagRelationMapper) {
        this.tagMapper = tagMapper;
        this.tagRelationMapper = tagRelationMapper;
    }

    @Override
    public List<DiaryTag> getUserTags(Long userId) {
        return tagMapper.selectByUserId(userId);
    }

    @Override
    public List<DiaryTag> getDiaryTags(Long diaryId) {
        return tagMapper.selectByDiaryId(diaryId);
    }

    @Override
    public List<DiaryTag> getPopularTags(Long userId, int limit) {
        List<DiaryTag> allTags = tagMapper.selectByUserId(userId);
        return allTags.stream()
                .sorted((a, b) -> b.getUseCount().compareTo(a.getUseCount()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DiaryTag createOrUpdateTag(String tagName, Long userId) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return null;
        }

        String normalizedTag = normalizeTag(tagName);
        DiaryTag existingTag = tagMapper.selectByUserIdAndName(userId, normalizedTag);

        if (existingTag != null) {
            // 标签已存在，增加使用次数
            tagMapper.incrementUseCount(existingTag.getId());
            existingTag.setUseCount(existingTag.getUseCount() + 1);
            log.debug("标签已存在，增加使用次数: {}", normalizedTag);
            return existingTag;
        } else {
            // 创建新标签
            DiaryTag newTag = new DiaryTag();
            newTag.setUserId(userId);
            newTag.setName(normalizedTag);
            newTag.setColor(generateTagColor());
            newTag.setUseCount(1);

            tagMapper.insert(newTag);
            log.info("创建新标签: {}, 用户ID: {}", normalizedTag, userId);
            return newTag;
        }
    }

    @Override
    @Transactional
    public void processTagsForDiary(List<String> tagNames, Long diaryId, Long userId) {
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }

        for (String tagName : tagNames) {
            DiaryTag tag = createOrUpdateTag(tagName, userId);
            if (tag != null) {
                // 创建关联关系
                DiaryTagRelation relation = new DiaryTagRelation();
                relation.setDiaryId(diaryId);
                relation.setTagId(tag.getId());
                tagRelationMapper.insert(relation);
            }
        }

        log.debug("为日记处理标签完成, 日记ID: {}, 标签数量: {}", diaryId, tagNames.size());
    }

    @Override
    @Transactional
    public void updateDiaryTags(List<String> newTagNames, Long diaryId, Long userId) {
        // 获取现有的标签关联
        List<DiaryTagRelation> existingRelations = tagRelationMapper.selectByDiaryId(diaryId);
        List<DiaryTag> existingTags = tagMapper.selectByDiaryId(diaryId);

        Set<String> existingTagNames = existingTags.stream()
                .map(DiaryTag::getName)
                .collect(Collectors.toSet());

        Set<String> newTagNameSet = new HashSet<>(newTagNames);

        // 找出需要删除的标签
        for (DiaryTagRelation relation : existingRelations) {
            DiaryTag tag = tagMapper.selectById(relation.getTagId());
            if (!newTagNameSet.contains(tag.getName())) {
                // 删除关联
                tagRelationMapper.deleteByDiaryAndTag(diaryId, tag.getId());
                // 减少标签使用计数
                tagMapper.decrementUseCount(tag.getId());
                log.debug("删除标签关联: 日记ID={}, 标签={}", diaryId, tag.getName());
            }
        }

        // 找出需要添加的标签
        for (String tagName : newTagNames) {
            if (!existingTagNames.contains(tagName)) {
                DiaryTag tag = createOrUpdateTag(tagName, userId);
                if (tag != null) {
                    // 创建关联关系
                    DiaryTagRelation relation = new DiaryTagRelation();
                    relation.setDiaryId(diaryId);
                    relation.setTagId(tag.getId());
                    tagRelationMapper.insert(relation);
                    log.debug("添加标签关联: 日记ID={}, 标签={}", diaryId, tag.getName());
                }
            }
        }

        log.info("更新日记标签完成, 日记ID: {}, 用户ID: {}", diaryId, userId);
    }

    @Override
    @Transactional
    public void deleteUnusedTags(Long userId) {
        List<DiaryTag> userTags = tagMapper.selectByUserId(userId);
        for (DiaryTag tag : userTags) {
            int usageCount = tagRelationMapper.countByTagId(tag.getId());
            if (usageCount == 0) {
                tagMapper.deleteById(tag.getId());
                log.debug("删除未使用标签: {}", tag.getName());
            }
        }
    }

    @Override
    public List<String> extractTagsFromString(String tagString) {
        if (tagString == null || tagString.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.stream(tagString.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .map(this::normalizeTag)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> getAllUserTagNames(Long userId) {
        List<String> allTags = tagMapper.selectAllUserTagNames(userId);
        return new HashSet<>(allTags);
    }

    @Override
    public List<DiaryTag> searchTagsByKeyword(Long userId, String keyword) {
        List<DiaryTag> userTags = tagMapper.selectByUserId(userId);
        return userTags.stream()
                .filter(tag -> tag.getName().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public int getUserTagCount(Long userId) {
        return tagMapper.countByUserId(userId);
    }

    private String normalizeTag(String tag) {
        return tag.trim().toLowerCase();
    }

    private String generateTagColor() {
        // 预定义一些好看的标签颜色
        String[] colors = {
                "#1890ff", "#52c41a", "#faad14", "#f5222d", "#722ed1",
                "#fa541c", "#13c2c2", "#eb2f96", "#a0d911", "#2f54eb"
        };
        Random random = new Random();
        return colors[random.nextInt(colors.length)];
    }
}