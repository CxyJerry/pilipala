package com.jerry.pilipala.infrastructure.enums;

import lombok.Getter;

@Getter
public enum MessageStatusEnum {
    UNREAD("未读"),
    READ("已读"),
    DELETED("已删除");
    private final String status;

    MessageStatusEnum(String status) {
        this.status = status;
    }
}
