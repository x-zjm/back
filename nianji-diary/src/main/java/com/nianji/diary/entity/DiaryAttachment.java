package com.nianji.diary.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.nianji.common.entity.BaseEntity;
import lombok.*;


import java.time.LocalDateTime;


/**
 * 日记附件实体类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("diary_attachment")
public class DiaryAttachment extends BaseEntity {

    /**
     * 日记ID
     */
    @TableField(value = "diary_id")
    private Long diaryId;

    /**
     * 存储的文件名（UUID）
     */
    @TableField(value = "file_name")
    private String fileName;

    /**
     * 原始文件名
     */
    @TableField(value = "original_file_name")
    private String originalFileName;

    /**
     * 文件存储路径
     */
    @TableField(value = "file_path")
    private String filePath;

    /**
     * 文件大小（字节）
     */
    @TableField(value = "file_size")
    private Long fileSize;

    /**
     * 文件类型（image/video）
     */
    @TableField(value = "file_type")
    private String fileType;

    /**
     * MIME类型
     */
    @TableField(value = "mime_type")
    private String mimeType;

    /**
     * 文件描述
     */
    @TableField(value = "mime_type")
    private String description;

    /**
     * 排序字段
     */
    @TableField(value = "sort_order")
    private Integer sortOrder;

    public String getFileExtension() {
        if (originalFileName != null && originalFileName.contains(".")) {
            return originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    public boolean isImage() {
        return "image".equals(fileType) || (mimeType != null && mimeType.startsWith("image/"));
    }

    public boolean isVideo() {
        return "video".equals(fileType) || (mimeType != null && mimeType.startsWith("video/"));
    }

    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";

        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024 * 1024));
        }
    }
}