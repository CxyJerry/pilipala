package com.jerry.pilipala.infrastructure.enums.audio;

import lombok.Getter;

@Getter
public enum AudioCodecEnum {
    mp3("mp3"),
    aac("aac");
    private final String value;

    AudioCodecEnum(String value) {
        this.value = value;
    }
}
