package com.jerry.pilipala.infrastructure.enums.video;

import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import lombok.Getter;

@Getter
public enum VodInteractiveActionEnum {
    LIKE("like"),
    COIN("coin"),
    COLLECT("collect"),
    SHARE("share");
    private final String name;

    VodInteractiveActionEnum(String name) {
        this.name = name;
    }

    public static VodInteractiveActionEnum parse(String name) {
        for (VodInteractiveActionEnum value : VodInteractiveActionEnum.values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        throw BusinessException.businessError("交互动作不存在");
    }
}
