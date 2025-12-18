package com.nianji.auth.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nianji.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


import java.time.LocalDateTime;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("login_logs")
public class LoginLog extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 用户名
     */
    @TableField(value = "username")
    private String username;

    /**
     * 登录IP
     */
    @TableField(value = "login_ip")
    private String loginIp;

    /**
     * 登录地点
     */
    @TableField(value = "login_location")
    private String loginLocation;

    /**
     * 用户代理
     */
    @TableField(value = "user_agent")
    private String userAgent;

    /**
     * 登录时间
     */
    @TableField(value = "login_time")
    private LocalDateTime loginTime;

    /**
     * 登录状态：0-失败，1-成功 see LoginStatusEnum
     */
    @TableField(value = "login_status")
    private Integer loginStatus;

    /**
     * 失败原因
     */
    @TableField(value = "fail_reason")
    private String failReason;
}