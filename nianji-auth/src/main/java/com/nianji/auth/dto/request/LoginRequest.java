package com.nianji.auth.dto.request;


import com.nianji.common.reqres.Command;
import com.nianji.common.assertion.ParamAssert;
import com.nianji.common.errorcode.ErrorCode;
import lombok.Data;

@Data
public class LoginRequest implements Command {
    
    private String username;
    
    private String password;
    
    @Override
    public void validate() {
        ParamAssert.notBlank(username, ErrorCode.Client.PARAM_NULL, "用户名不能为空");
        ParamAssert.notBlank(password, ErrorCode.Client.PARAM_NULL, "密码不能为空");
    }
}