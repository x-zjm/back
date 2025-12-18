package com.nianji.diary.controller;


import com.nianji.diary.entity.DiaryAttachment;
import com.nianji.diary.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/files")
public class FileController {


    private final FileService fileService;

    @Value("${app.upload.path}")
    private String uploadBasePath;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/{userId}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable Long userId,
            @PathVariable String filename) {

        try {
            Path filePath = Paths.get(fileService.getFileStorePath(userId)).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // 确定内容类型
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("文件访问失败: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/download/{attachmentId}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long attachmentId,
            @RequestHeader("X-User-Id") Long userId) {

        try {
            DiaryAttachment attachment = fileService.getAttachmentById(attachmentId, userId);
            if (attachment == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(attachment.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + attachment.getOriginalFileName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/validate")
    public Result<Void> validateFile(@RequestParam("file") MultipartFile file) {
        try {
            fileService.validateFile(file);
            return Result.success();
        } catch (Exception e) {
            log.error("文件验证失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/diary/{diaryId}")
    public Result<List<DiaryAttachment>> getDiaryAttachments(
            @PathVariable Long diaryId,
            @RequestHeader("X-User-Id") Long userId) {

        try {
            List<DiaryAttachment> attachments = fileService.getAttachmentsByDiaryId(diaryId);
            return Result.success(attachments);
        } catch (Exception e) {
            log.error("获取日记附件失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{attachmentId}")
    public Result<Void> deleteFile(
            @PathVariable Long attachmentId,
            @RequestHeader("X-User-Id") Long userId) {

        try {
            boolean deleted = fileService.deleteFile(attachmentId, userId);
            if (deleted) {
                return Result.success();
            } else {
                return Result.error("文件删除失败");
            }
        } catch (Exception e) {
            log.error("删除文件失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{attachmentId}/description")
    public Result<Void> updateAttachmentDescription(
            @PathVariable Long attachmentId,
            @RequestParam String description,
            @RequestHeader("X-User-Id") Long userId) {

        try {
            boolean updated = fileService.updateAttachmentDescription(attachmentId, description, userId);
            if (updated) {
                return Result.success();
            } else {
                return Result.error("更新附件描述失败");
            }
        } catch (Exception e) {
            log.error("更新附件描述失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/sort-order")
    public Result<Void> updateAttachmentSortOrder(
            @RequestBody List<Long> attachmentIds,
            @RequestHeader("X-User-Id") Long userId) {

        try {
            boolean updated = fileService.updateAttachmentSortOrder(attachmentIds, userId);
            if (updated) {
                return Result.success();
            } else {
                return Result.error("更新附件排序失败");
            }
        } catch (Exception e) {
            log.error("更新附件排序失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/upload")
    public Result<DiaryAttachment> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long diaryId,
            @RequestHeader("X-User-Id") Long userId) {

        try {
            DiaryAttachment attachment = fileService.uploadFile(file, diaryId, userId);
            return Result.success(attachment);
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}