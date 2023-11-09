package com.jerry.pilipala.infrastructure.enums;

import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import lombok.Getter;

@Getter
public enum ApplyStatusEnum {
    WAITING("等待处理中"),
    PROCESSED("已处理");
    private final String status;

    ApplyStatusEnum(String status) {
        this.status = status;
    }

    public static ApplyStatusEnum parse(String status) {
        for (ApplyStatusEnum value : ApplyStatusEnum.values()) {
            if (value.status.equals(status)) {
                return value;
            }
        }
        throw BusinessException.businessError("状态不存在");
    }
}
