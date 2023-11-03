package com.jerry.pilipala.infrastructure.enums;

import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import lombok.Getter;

@Getter
public enum VodStatusEnum {
    DRAFT("draft", "草稿"),
    INIT("init", "初始化"),
    HANDING("handing", "处理中"),

    PASSED("passed", "已过审"),
    FAIL("fail", "未过审");
    private final String status;
    private final String desc;

    VodStatusEnum(String status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static VodStatusEnum parse(String status) {
        for (VodStatusEnum value : VodStatusEnum.values()) {
            if (value.status.equals(status)) {
                return value;
            }
        }
        throw new BusinessException("稿件状态不存在", StandardResponse.ERROR);
    }
}
