package com.nianji.diary.dao.repository;

import com.nianji.diary.entity.Tag;

/**
 * TODO
 *
 * @author zhangjinming
 * @version 0.0.1
 */
public interface DiaryTagRepository {

    /**
     * 通过标签名称和所有者Id查询标签
     *
     * @param tagName
     *         标签名称
     * @param userId
     *         所有者Id
     * @return 返回查询到的标签
     */
    Tag selectOneByNameAndUserId(String tagName, Long userId);

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
}
