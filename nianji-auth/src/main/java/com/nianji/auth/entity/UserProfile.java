package com.nianji.auth.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nianji.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;


import java.time.LocalDate;


@Data
// @SuperBuilder
@Builder
// @EqualsAndHashCode(callSuper = true)
@TableName("user_profiles")
public class UserProfile {
    
    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;
    
    /**
     * 性别：0-未知，1-男，2-女
     */
    @TableField(value = "gender")
    private Integer gender;
    
    /**
     * 生日
     */
    @TableField(value = "birthday")
    private LocalDate birthday;
    
}