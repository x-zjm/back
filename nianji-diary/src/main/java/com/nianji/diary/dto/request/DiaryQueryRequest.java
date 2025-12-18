package com.nianji.diary.dto.request;


import lombok.Data;


import java.util.List;


@Data
public class DiaryQueryRequest {
    
    private String keyword;
    
    private List<Long> tagIds;
    
    private Integer page = 1;
    
    private Integer size = 20;
}