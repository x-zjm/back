package com.nianji.diary.dao.mapper;

import com.nianji.common.mybatis.CustomBaseMapper;
import com.nianji.diary.entity.DiaryAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;


import java.util.List;


@Mapper
public interface DiaryAttachmentMapper extends CustomBaseMapper<DiaryAttachment> {

    @Select("SELECT * FROM diary_attachment WHERE diary_id = #{diaryId} ORDER BY sort_order ASC, create_time ASC")
    List<DiaryAttachment> selectByDiaryId(@Param("diaryId") Long diaryId);

    @Select("SELECT * FROM diary_attachment WHERE diary_id = #{diaryId} AND file_type = #{fileType} ORDER BY sort_order ASC, create_time ASC")
    List<DiaryAttachment> selectByDiaryIdAndType(@Param("diaryId") Long diaryId, @Param("fileType") String fileType);

    @Select("SELECT * FROM diary_attachment WHERE diary_id = #{diaryId} AND file_type = 'image' ORDER BY sort_order ASC, create_time ASC")
    List<DiaryAttachment> selectImagesByDiaryId(@Param("diaryId") Long diaryId);

    @Select("SELECT * FROM diary_attachment WHERE diary_id = #{diaryId} AND file_type = 'video' ORDER BY sort_order ASC, create_time ASC")
    List<DiaryAttachment> selectVideosByDiaryId(@Param("diaryId") Long diaryId);

    @Delete("DELETE FROM diary_attachment WHERE diary_id = #{diaryId}")
    int deleteByDiaryId(@Param("diaryId") Long diaryId);

    @Select("SELECT * FROM diary_attachment WHERE file_path = #{filePath}")
    DiaryAttachment selectByFilePath(@Param("filePath") String filePath);

    @Select("SELECT da.* FROM diary_attachment da " +
            "JOIN diary d ON da.diary_id = d.id " +
            "WHERE d.user_id = #{userId} ORDER BY da.create_time DESC")
    List<DiaryAttachment> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM diary_attachment WHERE diary_id = #{diaryId}")
    int countByDiaryId(@Param("diaryId") Long diaryId);

    @Select("SELECT COUNT(*) FROM diary_attachment WHERE diary_id = #{diaryId} AND file_type = 'image'")
    int countImagesByDiaryId(@Param("diaryId") Long diaryId);

    @Select("SELECT COUNT(*) FROM diary_attachment WHERE diary_id = #{diaryId} AND file_type = 'video'")
    int countVideosByDiaryId(@Param("diaryId") Long diaryId);

    @Select("UPDATE diary_attachment SET sort_order = #{sortOrder} WHERE id = #{attachmentId}")
    int updateSortOrder(@Param("attachmentId") Long attachmentId, @Param("sortOrder") Integer sortOrder);
}