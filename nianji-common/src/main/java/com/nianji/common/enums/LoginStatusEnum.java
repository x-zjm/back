package com.nianji.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum LoginStatusEnum {

    FAIL(0, "失败"),
    SUCCESS(1, "成功"),
    ;

    private final Integer code;
    private final String desc;

    public static LoginStatusEnum getByCode(Integer code) {
        for (LoginStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return FAIL;
    }
}