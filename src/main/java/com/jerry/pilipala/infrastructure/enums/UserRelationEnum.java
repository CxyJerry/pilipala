package com.jerry.pilipala.infrastructure.enums;

import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import lombok.Getter;

@Getter
public enum UserRelationEnum {
    UNFOLLOW(0),
    FOLLOW(1);
    private final int code;

    UserRelationEnum(int code) {
        this.code = code;
    }

    public static UserRelationEnum parse(int code) {
        for (UserRelationEnum value : UserRelationEnum.values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw BusinessException.businessError("关系状态不存在");
    }
}
