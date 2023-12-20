package com.jerry.pilipala.infrastructure.enums.message;

import lombok.Getter;

@Getter
public enum MessageType {
    REPLY("reply", "回复"),
    AT("at", "@"),
    LIKE("like", "点赞"),
    SYSTEM("system", "系统"),
    WHISPER("whisper", "私信");
    private final String type;
    private final String desc;

    MessageType(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
