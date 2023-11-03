package com.jerry.pilipala.infrastructure.enums;

import lombok.Getter;

@Getter
public enum FrameRateEnum {
    _60(60),
    _30(30);
    private final int count;

    FrameRateEnum(int count) {
        this.count = count;
    }

    public int count() {
        return count;
    }
}
