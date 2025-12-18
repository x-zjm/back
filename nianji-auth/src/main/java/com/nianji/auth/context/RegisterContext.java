package com.nianji.auth.context;

import com.nianji.auth.dto.request.RegisterRequest;
import com.nianji.auth.model.cache.CacheCheckResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册相关上下文
 *
 * @author zhangjinming
 * @version 0.0.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterContext {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 缓存检查结果
     */
    private CacheCheckResult cacheResult;

    public static RegisterContext buildRegisterContext(RegisterRequest registerRequest) {
        return RegisterContext.builder()
                .username(registerRequest.getUsername().trim().toLowerCase())
                .password(registerRequest.getPassword().trim().toLowerCase())
                .email(registerRequest.getEmail().trim().toLowerCase())
                .phone(registerRequest.getPhone() != null ? registerRequest.getPhone().trim() : null)
                .build();
    }
}
