package com.nianji.diary.dto.request;


import lombok.Data;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;


@Data
public class DiaryUpdateRequest {
    
    @NotNull(message = "日记ID不能为空")
    private Long id;
    
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;
    
    @NotBlank(message = "内容不能为空")
    private String content;
    
    private String weather;
    
    private String mood;
    
    private String location;
    
    @NotNull(message = "是否公开不能为空")
    private Boolean isPublic;
    
    @Size(max = 10, message = "标签数量不能超过10个")
    private List<String> tagNames;
}