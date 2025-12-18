package com.nianji.diary.dao.mapper;

import com.nianji.common.mybatis.CustomBaseMapper;
import com.nianji.diary.entity.DiaryTagRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;


import java.util.List;


@Mapper
public interface DiaryTagRelationMapper extends CustomBaseMapper<DiaryTagRelation> {

    @Select("SELECT dtr.* FROM diary_tag_relation dtr WHERE dtr.diary_id = #{diaryId}")
    List<DiaryTagRelation> selectByDiaryId(@Param("diaryId") Long diaryId);

    @Select("SELECT dtr.* FROM diary_tag_relation dtr WHERE dtr.tag_id = #{tagId}")
    List<DiaryTagRelation> selectByTagId(@Param("tagId") Long tagId);

    @Select("SELECT dtr.* FROM diary_tag_relation dtr " +
            "JOIN diary d ON dtr.diary_id = d.id " +
            "WHERE d.user_id = #{userId} AND dtr.tag_id = #{tagId}")
    List<DiaryTagRelation> selectByUserIdAndTagId(@Param("userId") Long userId, @Param("tagId") Long tagId);

    @Delete("DELETE FROM diary_tag_relation WHERE diary_id = #{diaryId}")
    int deleteByDiaryId(@Param("diaryId") Long diaryId);

    @Delete("DELETE FROM diary_tag_relation WHERE diary_id = #{diaryId} AND tag_id = #{tagId}")
    int deleteByDiaryAndTag(@Param("diaryId") Long diaryId, @Param("tagId") Long tagId);

    @Select("SELECT COUNT(*) FROM diary_tag_relation WHERE tag_id = #{tagId}")
    int countByTagId(@Param("tagId") Long tagId);
}