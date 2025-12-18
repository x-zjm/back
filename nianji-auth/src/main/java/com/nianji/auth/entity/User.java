package com.nianji.auth.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.nianji.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;


import java.time.LocalDateTime;


@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class User extends BaseEntity {

    /**
     * 对外公开id
     */
    @TableField(value = "public_id")
    private String publicId;

    /**
     * 用户名
     */
    @TableField(value = "username")
    private String username;

    /**
     * 密码
     */
    @TableField(value = "password")
    private String password;

    /**
     * 邮箱
     */
    @TableField(value = "email")
    private String email;

    /**
     * 手机号
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 昵称
     */
    @TableField(value = "nickname")
    private String nickname;

    /**
     * 头像
     */
    @TableField(value = "avatar")
    private String avatar;

    /**
     * 状态：0-禁用，1-正常，2-锁定，see UserStatusEnum
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 最后登录时间
     */
    @TableField(value = "last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录Ip
     */
    @TableField(value = "last_login_ip")
    private String lastLoginIp;

    /**
     * 登录次数
     */
    @TableField(value = "login_count")
    private Integer loginCount;

}