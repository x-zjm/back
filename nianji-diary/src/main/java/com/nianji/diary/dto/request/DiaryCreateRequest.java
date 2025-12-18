package com.nianji.diary.dto.request;


import cn.hutool.core.util.ObjectUtil;
import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.BusinessAssert;
import com.nianji.common.reqres.Command;
import lombok.Data;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Data
public class DiaryCreateRequest implements Command {

    private String title;

    private String content;

    private String weather;

    private String mood;

    private String location;

    private Boolean isPublic;

    List<MultipartFile> files;

    private List<String> tagNames;

    @Override
    public void validate() {
        BusinessAssert.notBlank(title, ErrorCode.Client.PARAM_NULL, "标题不能为空");
        BusinessAssert.isTrue(title.length() <= 10, ErrorCode.Client.PARAM_ERROR,
                "标题长度不能超过10个字符");
        BusinessAssert.notBlank(content, ErrorCode.Client.PARAM_NULL, "内容不能为空");
        BusinessAssert.isTrue(ObjectUtil.isNotNull(isPublic), ErrorCode.Client.PARAM_NULL,
                "是否公开不能为空");
        BusinessAssert.isTrue(tagNames.size() <= 10, ErrorCode.Client.PARAM_ERROR,
                "标签数量不能超过10个");
    }
}