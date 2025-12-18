package com.nianji.auth.vo;


import lombok.Data;


import java.time.LocalDateTime;


@Data
public class UserInfoVO {
    
    private Long userId;
    
    private String username;
    
    private String email;
    
    private String phone;
    
    private String nickname;
    
    private String avatar;
    
    private Integer status;
    
    private Integer gender;
    
    private String birthday;
    
    private String bio;
    
    private String location;
    
    private String website;
    
    private LocalDateTime lastLoginTime;
    
    private Integer loginCount;
    
    private LocalDateTime createTime;
}