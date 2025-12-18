package com.nianji.diary.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.nianji.common.entity.BaseEntity;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("diary_favorite")
public class DiaryFavorite extends BaseEntity {

    /**
     * 日记ID
     */
    @TableField(value = "diary_id")
    private Long diaryId;

    /**
     * 收藏者用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 通过哪个分享收藏的
     */
    @TableField(value = "share_id")
    private Long shareId;

}