package com.nianji.diary.vo;


import lombok.Data;


import java.time.LocalDateTime;


@Data
public class TagVO {
    
    private Long id;
    
    private String name;
    
    private String color;
    
    private Long userId;
    
    private Integer diaryCount;
    
    private LocalDateTime createTime;
}