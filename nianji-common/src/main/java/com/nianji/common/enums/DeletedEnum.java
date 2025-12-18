package com.nianji.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum DeletedEnum {

    NOT_DELETED(0, "未删除的"),
    DELETED(1, "删除的");

    private final Integer code;
    private final String desc;

    public static DeletedEnum getByCode(Integer code) {
        for (DeletedEnum delete : values()) {
            if (delete.getCode().equals(code)) {
                return delete;
            }
        }
        return DELETED;
    }
}