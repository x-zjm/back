package com.nianji.auth.dto.request;


import cn.hutool.core.util.ObjectUtil;
import com.nianji.common.reqres.Command;
import com.nianji.common.assertion.ParamAssert;
import com.nianji.common.errorcode.ErrorCode;
import lombok.Data;


@Data
public class ResetPasswordRequest implements Command {

    private String oldPassword;

    private String newPassword;


    @Override
    public void validate() {
        ParamAssert.notBlank(oldPassword, ErrorCode.Client.PARAM_NULL, "旧密码不能为空");
        ParamAssert.notBlank(newPassword, ErrorCode.Client.PARAM_NULL, "新密码不能为空");

        // 新旧密码不能一致
        // ParamAssert.isTrue(ObjectUtil.notEqual(oldPassword, newPassword), ErrorCode.Client.PARAM_ERROR, "新旧密码不能一致");

        // 密码强度校验
        // ParamAssert.isTrue(newPassword.length() >= 8, ErrorCode.Client.PARAM_ERROR, "密码长度至少8位");
        // NjAssert.isTrue(password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$"), ErrorCode.Client.PARAM_INVALID, "密码必须包含大小写字母和数字");
        // NjAssert.isTrue(!isCommonWeakPassword(password), ErrorCode.Client.PARAM_INVALID, "密码过于简单，请使用更复杂的密码");
    }
}