// diary/src/main/java/com/nianji/diary/service/impl/FileServiceImpl.java
package com.nianji.diary.service.impl;


import com.nianji.diary.entity.Diary;
import com.nianji.diary.entity.DiaryAttachment;
import com.nianji.diary.mapper.DiaryAttachmentMapper;
import com.nianji.diary.mapper.DiaryMapper;
import com.nianji.diary.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 文件服务实现类
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {


    @Value("${app.upload.path}")
    private String uploadBasePath;

    @Value("${app.upload.image-max-size}")
    private long imageMaxSize;

    @Value("${app.upload.video-max-size}")
    private long videoMaxSize;

    @Value("${app.upload.allowed-image-types}")
    private String allowedImageTypes;

    @Value("${app.upload.allowed-video-types}")
    private String allowedVideoTypes;

    private final DiaryAttachmentMapper attachmentMapper;
    private final DiaryMapper diaryMapper;

    // 允许的文件类型集合
    private Set<String> allowedImageTypeSet;
    private Set<String> allowedVideoTypeSet;

    public FileServiceImpl(DiaryAttachmentMapper attachmentMapper, DiaryMapper diaryMapper) {
        this.attachmentMapper = attachmentMapper;
        this.diaryMapper = diaryMapper;
        initializeAllowedTypes();
    }

    private void initializeAllowedTypes() {
        allowedImageTypeSet = Arrays.stream(allowedImageTypes.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        allowedVideoTypeSet = Arrays.stream(allowedVideoTypes.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    @Override
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        long fileSize = file.getSize();

        log.debug("验证文件: {}, 类型: {}, 大小: {} bytes", originalFilename, contentType, fileSize);

        // 检查文件类型
        if (contentType == null) {
            throw new IllegalArgumentException("无法识别文件类型");
        }

        if (contentType.startsWith("image/")) {
            if (!allowedImageTypeSet.contains(contentType)) {
                throw new IllegalArgumentException("不支持的图片格式: " + contentType + "，支持格式: " + allowedImageTypes);
            }
            if (fileSize > imageMaxSize) {
                throw new IllegalArgumentException("图片大小不能超过 " + (imageMaxSize / 1024 / 1024) + "MB");
            }
        } else if (contentType.startsWith("video/")) {
            if (!allowedVideoTypeSet.contains(contentType)) {
                throw new IllegalArgumentException("不支持的视频格式: " + contentType + "，支持格式: " + allowedVideoTypes);
            }
            if (fileSize > videoMaxSize) {
                throw new IllegalArgumentException("视频大小不能超过 " + (videoMaxSize / 1024 / 1024) + "MB");
            }
        } else {
            throw new IllegalArgumentException("不支持的文件类型: " + contentType + "，仅支持图片和视频文件");
        }
    }

    @Override
    public DiaryAttachment uploadFile(MultipartFile file, Long diaryId, Long userId) throws IOException {
        return null;
    }

    @Override
    public List<DiaryAttachment> uploadFiles(List<MultipartFile> files, Long diaryId, Long userId) {
        return null;
    }

    @Override
    public List<DiaryAttachment> getAttachmentsByDiaryId(Long diaryId) {
        return null;
    }

    @Override
    public List<DiaryAttachment> getAttachmentsByDiaryIdAndType(Long diaryId, String fileType) {
        return null;
    }

    @Override
    public List<DiaryAttachment> getImageAttachmentsByDiaryId(Long diaryId) {
        return null;
    }

    @Override
    public List<DiaryAttachment> getVideoAttachmentsByDiaryId(Long diaryId) {
        return null;
    }

    @Override
    public String getFileStorePath(Long userId) {
        return null;
    }

    @Override
    public boolean deleteFile(Long attachmentId, Long userId) {
        return false;
    }

    @Override
    public void deleteFilesByDiaryId(Long diaryId, Long userId) {

    }

    @Override
    public boolean updateAttachmentDescription(Long attachmentId, String description, Long userId) {
        return false;
    }

    @Override
    public boolean updateAttachmentSortOrder(List<Long> attachmentIds, Long userId) {
        return false;
    }

    @Override
    public DiaryAttachment getAttachmentById(Long attachmentId, Long userId) {
        return null;
    }

    @Override
    public boolean hasAttachmentPermission(Long attachmentId, Long userId) {
        return false;
    }
}