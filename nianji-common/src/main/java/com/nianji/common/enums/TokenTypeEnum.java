package com.nianji.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum TokenTypeEnum {

    BEARER("Bearer", "持有者"),
    ;

    private final String type;
    private final String desc;

    public static TokenTypeEnum getByType(String type) {
        for (TokenTypeEnum tokenType : values()) {
            if (tokenType.getType().equals(type)) {
                return tokenType;
            }
        }
        return BEARER;
    }
}