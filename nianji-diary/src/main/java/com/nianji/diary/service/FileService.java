// diary/src/main/java/com/nianji/diary/service/FileService.java
package com.nianji.diary.service;


import com.nianji.diary.entity.DiaryAttachment;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;


/**
 * 文件服务接口
 */
public interface FileService {
    
    /**
     * 验证文件
     */
    void validateFile(MultipartFile file) throws IllegalArgumentException;
    
    /**
     * 上传单个文件
     */
    DiaryAttachment uploadFile(MultipartFile file, Long diaryId, Long userId) throws IOException;
    
    /**
     * 批量上传文件
     */
    List<DiaryAttachment> uploadFiles(List<MultipartFile> files, Long diaryId, Long userId);
    
    /**
     * 根据日记ID获取附件列表
     */
    List<DiaryAttachment> getAttachmentsByDiaryId(Long diaryId);
    
    /**
     * 根据日记ID和文件类型获取附件
     */
    List<DiaryAttachment> getAttachmentsByDiaryIdAndType(Long diaryId, String fileType);
    
    /**
     * 获取日记的所有图片附件
     */
    List<DiaryAttachment> getImageAttachmentsByDiaryId(Long diaryId);
    
    /**
     * 获取日记的所有视频附件
     */
    List<DiaryAttachment> getVideoAttachmentsByDiaryId(Long diaryId);
    
    /**
     * 获取文件存储路径
     */
    String getFileStorePath(Long userId);
    
    /**
     * 删除文件
     */
    boolean deleteFile(Long attachmentId, Long userId);
    
    /**
     * 根据日记ID删除所有附件
     */
    void deleteFilesByDiaryId(Long diaryId, Long userId);
    
    /**
     * 更新附件描述
     */
    boolean updateAttachmentDescription(Long attachmentId, String description, Long userId);
    
    /**
     * 更新附件排序
     */
    boolean updateAttachmentSortOrder(List<Long> attachmentIds, Long userId);
    
    /**
     * 获取附件信息
     */
    DiaryAttachment getAttachmentById(Long attachmentId, Long userId);
    
    /**
     * 检查用户是否有权限访问附件
     */
    boolean hasAttachmentPermission(Long attachmentId, Long userId);
}