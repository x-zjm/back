package com.nianji.auth.model.cache;

import lombok.Data;

/**
 * 缓存检查结果
 */
@Data
public class CacheCheckResult {
    private boolean usernameExists = false;
    private boolean emailExists = false;
    private boolean phoneExists = false;

    public boolean hasConflict() {
        return usernameExists || emailExists || phoneExists;
    }

    public String getConflictMessage() {
        if (usernameExists) return "用户名已存在";
        if (emailExists) return "邮箱已被注册";
        if (phoneExists) return "手机号已被注册";
        return null;
    }
}