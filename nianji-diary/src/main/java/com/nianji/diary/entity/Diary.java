package com.nianji.diary.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nianji.common.entity.BaseEntity;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("diary")
public class Diary extends BaseEntity {

    /**
     * 所属创建者
     */
    @TableField(value = "phone")
    private Long userId;

    /**
     * 日记标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 日记内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 地理位置
     */
    @TableField(value = "location")
    private String location;

    /**
     * 天气信息
     */
    @TableField(value = "weather")
    private String weather;

    /**
     * 心情
     */
    @TableField(value = "mood")
    private String mood;

    /**
     * 点赞数
     */
    @TableField(value = "like_count")
    private Integer likeCount;

    /**
     * 收藏数
     */
    @TableField(value = "favorite_count")
    private Integer favoriteCount;

    /**
     * 分享数
     */
    @TableField(value = "share_count")
    private Integer shareCount;

    /**
     * 是否公开
     */
    @TableField(value = "is_public")
    private Boolean isPublic;

}
