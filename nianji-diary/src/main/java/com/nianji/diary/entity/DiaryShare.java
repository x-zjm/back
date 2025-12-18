package com.nianji.diary.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.nianji.common.entity.BaseEntity;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("diary_share")
public class DiaryShare extends BaseEntity {

    /**
     * 日记ID
     */
    @TableField(value = "diary_id")
    private Long diaryId;

    /**
     * 日记所有者ID
     */
    @TableField(value = "owner_user_id")
    private Long ownerUserId;

    /**
     * 分享者用户ID
     */
    @TableField(value = "sharer_user_id")
    private Long sharerUserId;

    /**
     * 分享码
     */
    @TableField(value = "share_code")
    private String shareCode;

    /**
     * 是否允许收藏
     */
    @TableField(value = "allow_collect")
    private Boolean allowCollect;

    /**
     * 是否允许再次分享
     */
    @TableField(value = "allow_reshare")
    private Boolean allowReshare;

    /**
     * 父分享ID
     */
    @TableField(value = "parent_share_id")
    private Long parentShareId;

    /**
     * 过期时间
     */
    @TableField(value = "expire_time")
    private LocalDateTime expireTime;

}