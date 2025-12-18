package com.nianji.diary.dao.mapper;

import com.nianji.common.mybatis.CustomBaseMapper;
import com.nianji.diary.entity.DiaryTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


import java.util.List;

@Mapper
public interface DiaryTagMapper extends CustomBaseMapper<DiaryTag> {

    @Select("SELECT * FROM diary_tag WHERE user_id = #{userId} ORDER BY use_count DESC, create_time DESC")
    List<DiaryTag> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM diary_tag WHERE user_id = #{userId} AND name = #{name}")
    DiaryTag selectByUserIdAndName(@Param("userId") Long userId, @Param("name") String name);

    @Select("SELECT dt.* FROM diary_tag dt " +
            "JOIN diary_tag_relation dtr ON dt.id = dtr.tag_id " +
            "WHERE dtr.diary_id = #{diaryId} ORDER BY dt.name")
    List<DiaryTag> selectByDiaryId(@Param("diaryId") Long diaryId);

    @Select("SELECT dt.* FROM diary_tag dt " +
            "JOIN diary_tag_relation dtr ON dt.id = dtr.tag_id " +
            "JOIN diary d ON dtr.diary_id = d.id " +
            "WHERE d.user_id = #{userId} " +
            "GROUP BY dt.id ORDER BY COUNT(dtr.id) DESC, dt.name")
    List<DiaryTag> selectPopularTagsByUserId(@Param("userId") Long userId);

    @Update("UPDATE diary_tag SET use_count = use_count + 1 WHERE id = #{tagId}")
    int incrementUseCount(@Param("tagId") Long tagId);

    @Update("UPDATE diary_tag SET use_count = use_count - 1 WHERE id = #{tagId}")
    int decrementUseCount(@Param("tagId") Long tagId);

    @Select("SELECT COUNT(*) FROM diary_tag WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT dt.name FROM diary_tag dt " +
            "JOIN diary_tag_relation dtr ON dt.id = dtr.tag_id " +
            "JOIN diary d ON dtr.diary_id = d.id " +
            "WHERE d.user_id = #{userId}")
    List<String> selectAllUserTagNames(@Param("userId") Long userId);
}