package com.nianji.diary.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.nianji.common.entity.BaseEntity;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("diary_like")
public class DiaryLike extends BaseEntity {

    /**
     * 日记ID
     */
    @TableField(value = "diary_id")
    private Long diaryId;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;
}