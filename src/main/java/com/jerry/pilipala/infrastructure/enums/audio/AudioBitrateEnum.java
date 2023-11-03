package com.jerry.pilipala.infrastructure.enums.audio;

import com.jerry.pilipala.infrastructure.utils.unit.KB;
import com.jerry.pilipala.infrastructure.utils.unit.Unit;
import lombok.Getter;

@Getter
public enum AudioBitrateEnum {
    _32K(KB.create(32)),
    _64K(KB.create(64)),
    _128K(KB.create(128));
    private final Unit<Integer> value;

    AudioBitrateEnum(Unit<Integer> value) {
        this.value = value;
    }

    public int value() {
        return value.value();
    }
}
