package com.nianji.diary.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.nianji.common.entity.BaseEntity;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("diary_tag_relation")
public class DiaryTagRelation extends BaseEntity {

    /**
     * 日记ID
     */
    @TableField(value = "diary_id")
    private Long diaryId;

    /**
     * 标签ID
     */
    @TableField(value = "tag_id")
    private Long tagId;

}