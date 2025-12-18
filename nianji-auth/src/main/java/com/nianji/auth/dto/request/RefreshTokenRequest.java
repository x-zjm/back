package com.nianji.auth.dto.request;

import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.assertion.ParamAssert;
import com.nianji.common.reqres.Command;
import lombok.Data;

/**
 * 刷新令牌入参
 *
 * @author zhangjinming
 * @version 0.0.1
 */
@Data
public class RefreshTokenRequest implements Command {

    private String refreshToken;

    @Override
    public void validate() {
        ParamAssert.notBlank(refreshToken, ErrorCode.Client.PARAM_NULL, "refreshToken不能为空");
    }
}