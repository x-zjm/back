package com.nianji.diary.dao.mapper;


import com.nianji.common.mybatis.CustomBaseMapper;
import com.nianji.diary.entity.DiaryFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


import java.util.List;


@Mapper
public interface DiaryFavoriteMapper extends CustomBaseMapper<DiaryFavorite> {
    
    @Select("SELECT df.*, d.* FROM diary_favorite df " +
            "LEFT JOIN diary d ON df.diary_id = d.id " +
            "WHERE df.user_id = #{userId} ORDER BY df.create_time DESC")
    List<DiaryFavorite> selectByUserIdWithDiary(@Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) FROM diary_favorite WHERE user_id = #{userId} AND diary_id = #{diaryId}")
    int existsByUserAndDiary(@Param("userId") Long userId, @Param("diaryId") Long diaryId);
    
    @Select("SELECT * FROM diary_favorite WHERE user_id = #{userId} AND diary_id = #{diaryId}")
    DiaryFavorite selectByUserAndDiary(@Param("userId") Long userId, @Param("diaryId") Long diaryId);
}