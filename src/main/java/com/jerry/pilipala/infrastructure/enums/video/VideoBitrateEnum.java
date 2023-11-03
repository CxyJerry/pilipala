package com.jerry.pilipala.infrastructure.enums.video;

import com.jerry.pilipala.infrastructure.utils.unit.KB;
import com.jerry.pilipala.infrastructure.utils.unit.MB;
import com.jerry.pilipala.infrastructure.utils.unit.Unit;
import lombok.Getter;

@Getter
public enum VideoBitrateEnum {
    _200K(KB.create(200)),
    _400K(KB.create(400)),
    _900K(KB.create(900)),
    _1200K(KB.create(1200)),
    _2M(MB.create(2)),
    _3M(MB.create(3)),
    _6M(MB.create(6)),
    _20M(MB.create(20)),
    _40M(MB.create(40)),
    _60M(MB.create(60));
    private final Unit<Integer> value;

    VideoBitrateEnum(Unit<Integer> value) {
        this.value = value;
    }

    public int value() {
        return value.value();
    }
}
