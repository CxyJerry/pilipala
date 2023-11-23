package com.jerry.pilipala.infrastructure.enums.video;

import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import lombok.Getter;

@Getter
public enum VodInteractiveActionEnum {
    UNKNOWN("unknown"),
    FOLLOW("follow"),
    UNFOLLOW("unfollow"),
    PLAY("play"),
    UPDATE_TIME("update_time"),
    LIKE("like"),
    CANCEL_LIKE("cancel_like"),
    COLLECT("collect"),
    CANCEL_COLLECT("cancel_collect"),
    SHARE("share"),
    COIN("coin"),
    COMMENT("comment"),
    DELETE_COMMENT("delete_comment"),
    BARRAGE("barrage"),
    LEAVE("leave");


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
