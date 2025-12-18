package com.nianji.diary.controller;


import cn.hutool.json.JSONUtil;
import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.reqres.RequestModel;
import com.nianji.common.reqres.Result;
import com.nianji.diary.dto.request.DiaryCreateRequest;
import com.nianji.diary.service.DiaryService;
import com.nianji.diary.vo.DiaryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Slf4j
@Validated
@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping
    public Result<Long> createDiary(RequestModel<DiaryCreateRequest> requestModel,
                                    @RequestHeader("X-User-Id") Long userId) {

        log.info("DiaryController createDiary request:{}", JSONUtil.toJsonStr(requestModel));

        RequestModel.checkReqModel(requestModel);
        DiaryCreateRequest diaryCreateRequest = requestModel.getRequestData();
        diaryCreateRequest.validate();

        try {
            Long diaryId = diaryService.createDiary(diaryCreateRequest, userId);
            log.info("DiaryController createDiary response:{}", JSONUtil.toJsonStr(requestModel));
            return Result.ok(diaryId);
        } catch (Exception e) {
            log.error("创建日记失败: {}", e.getMessage());
            return Result.fail(ErrorCode.Server.SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/{diaryId}")
    public Result<DiaryVO> getDiary(
            @PathVariable Long diaryId,
            @RequestHeader("X-User-Id") Long userId) {

        try {
            DiaryVO diaryVO = diaryService.getDiaryWithAttachments(diaryId, userId);
            return Result.success(diaryVO);
        } catch (Exception e) {
            log.error("获取日记失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @GetMapping
    public Result<List<DiaryVO>> getUserDiaries(@RequestHeader("X-User-Id") Long userId) {
        try {
            List<DiaryVO> diaries = diaryService.getUserDiaries(userId);
            return Result.success(diaries);
        } catch (Exception e) {
            log.error("获取用户日记列表失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/tag/{tagName}")
    public Result<List<DiaryVO>> getDiariesByTag(
            @PathVariable String tagName,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            List<DiaryVO> diaries = diaryService.getDiariesByTag(userId, tagName);
            return Result.success(diaries);
        } catch (Exception e) {
            log.error("按标签获取日记失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/search/tag")
    public Result<List<DiaryVO>> searchDiariesByTagKeyword(
            @RequestParam String keyword,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            List<DiaryVO> diaries = diaryService.searchDiariesByTagKeyword(userId, keyword);
            return Result.success(diaries);
        } catch (Exception e) {
            log.error("按标签关键词搜索日记失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{diaryId}")
    public Result<Void> updateDiary(
            @PathVariable Long diaryId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String weather,
            @RequestParam(required = false) String mood,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) List<MultipartFile> files,
            @RequestHeader("X-User-Id") Long userId) {

        try {
            Diary diary = new Diary();
            diary.setId(diaryId);
            diary.setTitle(title);
            diary.setContent(content);
            diary.setLocation(location);
            diary.setWeather(weather);
            diary.setMood(mood);
            diary.setIsPublic(isPublic);

            // 解析标签
            List<String> tagNames = parseTagString(tags);

            diaryService.updateDiary(diary, tagNames, files, userId);
            return Result.success();
        } catch (Exception e) {
            log.error("更新日记失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{diaryId}/tags")
    public Result<Void> updateDiaryTags(
            @PathVariable Long diaryId,
            @RequestParam String tags,
            @RequestHeader("X-User-Id") Long userId) {

        try {
            List<String> tagNames = parseTagString(tags);
            diaryService.updateDiaryTags(diaryId, tagNames, userId);
            return Result.success();
        } catch (Exception e) {
            log.error("更新日记标签失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{diaryId}")
    public Result<Void> deleteDiary(
            @PathVariable Long diaryId,
            @RequestHeader("X-User-Id") Long userId) {

        try {
            diaryService.deleteDiary(diaryId, userId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除日记失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    private List<String> parseTagString(String tags) {
        if (tags == null || tags.trim().isEmpty()) {
            return null;
        }

        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toList());
    }
}