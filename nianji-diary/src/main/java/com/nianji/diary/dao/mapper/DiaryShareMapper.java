package com.nianji.diary.dao.mapper;


import com.nianji.common.mybatis.CustomBaseMapper;
import com.nianji.diary.entity.DiaryShare;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


import java.util.List;


@Mapper
public interface DiaryShareMapper extends CustomBaseMapper<DiaryShare> {
    
    @Select("SELECT * FROM diary_share WHERE share_code = #{shareCode} AND (expire_time IS NULL OR expire_time > NOW())")
    DiaryShare selectValidByShareCode(@Param("shareCode") String shareCode);
    
    @Select("SELECT * FROM diary_share WHERE sharer_user_id = #{userId} ORDER BY create_time DESC")
    List<DiaryShare> selectBySharerUserId(@Param("userId") Long userId);
    
    @Select("SELECT * FROM diary_share WHERE diary_id = #{diaryId} ORDER BY create_time DESC")
    List<DiaryShare> selectByDiaryId(@Param("diaryId") Long diaryId);
    
    @Select("SELECT * FROM diary_share WHERE owner_user_id = #{userId} ORDER BY create_time DESC")
    List<DiaryShare> selectByOwnerUserId(@Param("userId") Long userId);
}