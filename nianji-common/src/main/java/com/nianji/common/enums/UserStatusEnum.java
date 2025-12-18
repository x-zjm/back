package com.nianji.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum UserStatusEnum {

    DISABLED(0, "禁用"),
    NORMAL(1, "正常"),
    LOCKED(2, "锁定");

    private final Integer code;
    private final String desc;

    public static UserStatusEnum getByCode(Integer code) {
        for (UserStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return DISABLED;
    }
}