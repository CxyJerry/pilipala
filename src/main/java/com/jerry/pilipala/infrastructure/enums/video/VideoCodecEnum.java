package com.jerry.pilipala.infrastructure.enums.video;

import lombok.Getter;

@Getter
public enum VideoCodecEnum {
    AVC("avc"),
    HEVC("hevc"),
    AV1("av1");
    private final String value;

    VideoCodecEnum(String value) {
        this.value = value;
    }
}
