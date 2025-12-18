package com.nianji.auth.context;

import com.nianji.auth.dto.request.ResetPasswordRequest;
import lombok.*;

/**
 * TODO
 *
 * @author zhangjinming
 * @version 0.0.1
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordContext {

    /**
     * 请求token
     */
    private String token;

    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    public static ResetPasswordContext buildResetPasswordContext(ResetPasswordRequest resetPasswordRequest, String token) {
        return ResetPasswordContext.builder()
                .token(token)
                .oldPassword(resetPasswordRequest.getOldPassword())
                .newPassword(resetPasswordRequest.getNewPassword())
                .build();
    }
}
