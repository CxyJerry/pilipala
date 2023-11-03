package com.jerry.pilipala.infrastructure.utils.unit;

public class MB extends KB {
    public MB(Integer count) {
        super(count);
    }

    @Override
    public Integer unit() {
        return super.unit() * 1000;
    }

    public static MB create(int count) {
        return new MB(count);
    }
}
