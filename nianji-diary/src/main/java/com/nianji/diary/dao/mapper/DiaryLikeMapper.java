package com.nianji.diary.dao.mapper;


import com.nianji.common.mybatis.CustomBaseMapper;
import com.nianji.diary.entity.DiaryLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface DiaryLikeMapper extends CustomBaseMapper<DiaryLike> {
    
    @Select("SELECT COUNT(*) FROM diary_like WHERE diary_id = #{diaryId} AND user_id = #{userId}")
    int existsByDiaryAndUser(@Param("diaryId") Long diaryId, @Param("userId") Long userId);
    
    @Select("SELECT * FROM diary_like WHERE diary_id = #{diaryId} AND user_id = #{userId}")
    DiaryLike selectByDiaryAndUser(@Param("diaryId") Long diaryId, @Param("userId") Long userId);
}