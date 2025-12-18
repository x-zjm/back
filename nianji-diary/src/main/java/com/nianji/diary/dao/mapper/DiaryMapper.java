package com.nianji.diary.dao.mapper;

import com.nianji.common.mybatis.CustomBaseMapper;
import com.nianji.diary.entity.Diary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
@Mapper
public interface DiaryMapper extends CustomBaseMapper<Diary> {

    @Select("SELECT * FROM diary WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Diary> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT d.* FROM diary d " +
            "JOIN diary_tag_relation dtr ON d.id = dtr.diary_id " +
            "JOIN diary_tag dt ON dtr.tag_id = dt.id " +
            "WHERE d.user_id = #{userId} AND dt.name = #{tagName} " +
            "ORDER BY d.create_time DESC")
    List<Diary> selectByUserIdAndTag(@Param("userId") Long userId, @Param("tagName") String tagName);

    @Select("SELECT d.* FROM diary d " +
            "WHERE d.user_id = #{userId} AND d.is_public = true " +
            "ORDER BY d.create_time DESC")
    List<Diary> selectPublicDiariesByUserId(@Param("userId") Long userId);

    @Select("SELECT d.* FROM diary d " +
            "JOIN diary_tag_relation dtr ON d.id = dtr.diary_id " +
            "WHERE dtr.tag_id = #{tagId} " +
            "ORDER BY d.create_time DESC")
    List<Diary> selectByTagId(@Param("tagId") Long tagId);

    @Select("SELECT d.* FROM diary d " +
            "JOIN diary_tag_relation dtr ON d.id = dtr.diary_id " +
            "JOIN diary_tag dt ON dtr.tag_id = dt.id " +
            "WHERE d.user_id = #{userId} AND dt.name LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY d.create_time DESC")
    List<Diary> searchByTagKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    @Update("UPDATE diary SET like_count = like_count + 1 WHERE id = #{diaryId}")
    int incrementLikeCount(@Param("diaryId") Long diaryId);

    @Update("UPDATE diary SET like_count = like_count - 1 WHERE id = #{diaryId}")
    int decrementLikeCount(@Param("diaryId") Long diaryId);

    @Update("UPDATE diary SET favorite_count = favorite_count + 1 WHERE id = #{diaryId}")
    int incrementFavoriteCount(@Param("diaryId") Long diaryId);

    @Update("UPDATE diary SET favorite_count = favorite_count - 1 WHERE id = #{diaryId}")
    int decrementFavoriteCount(@Param("diaryId") Long diaryId);

    @Update("UPDATE diary SET share_count = share_count + 1 WHERE id = #{diaryId}")
    int incrementShareCount(@Param("diaryId") Long diaryId);
}