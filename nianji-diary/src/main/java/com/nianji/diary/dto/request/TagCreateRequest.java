package com.nianji.diary.dto.request;


import lombok.Data;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


@Data
public class TagCreateRequest {
    
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称长度不能超过50个字符")
    private String name;
    
    private String color;
}