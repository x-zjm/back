package com.nianji.diary.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("diary_share_access")
public class DiaryShareAccess {

    /**
     * 分享ID
     */
    @TableField(value = "share_id")
    private Long shareId;

    /**
     * 访问用户ID
     */
    @TableField(value = "access_user_id")
    private Long accessUserId;

    /**
     * 访问时间
     */
    @TableField(value = "access_time", fill = FieldFill.INSERT)
    private LocalDateTime accessTime;

}