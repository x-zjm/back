package com.nianji.auth.dto.request;


import cn.hutool.core.util.ObjectUtil;
import com.nianji.common.reqres.Command;
import com.nianji.common.assertion.ParamAssert;
import com.nianji.common.errorcode.ErrorCode;
import lombok.Data;

@Data
public class RegisterRequest implements Command {

    private String username;

    private String password;

    private String repeatPassword;

    private String email;

    private String phone;

    private String nickname;

    private String avatar;

    @Override
    public void validate() {
        ParamAssert.notBlank(username, ErrorCode.Client.PARAM_NULL, "用户名不能为空");
        ParamAssert.notBlank(password, ErrorCode.Client.PARAM_NULL, "密码不能为空");
        ParamAssert.notBlank(email, ErrorCode.Client.PARAM_NULL, "邮箱不能为空");

        // 用户名长度校验
        ParamAssert.isTrue(username.length() >= 3 && username.length() < 10, ErrorCode.Client.PARAM_ERROR, "用户名长度必须在3-10个字符之间");

        // 密码强度校验
        ParamAssert.isTrue(password.length() >= 8, ErrorCode.Client.PARAM_ERROR, "密码长度至少8位");
        // NjAssert.isTrue(password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$"), ErrorCode.Client.PARAM_ERROR, "密码必须包含大小写字母和数字");
        // NjAssert.isTrue(!isCommonWeakPassword(password), ErrorCode.Client.PARAM_ERROR, "密码过于简单，请使用更复杂的密码");

        // 两次密码必须一致
        ParamAssert.isTrue(ObjectUtil.equal(password, repeatPassword), ErrorCode.Client.PARAM_ERROR, "确认密码必须一致");
    }

}