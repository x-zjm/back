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
@TableName("diary_tag")
public class DiaryTag extends BaseEntity {

    /**
     * 所属标签名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 所属创建者
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 颜色
     */
    @TableField(value = "color")
    private String color;

    /**
     * 使用次数
     */
    @TableField(value = "use_count")
    private Integer useCount;
}