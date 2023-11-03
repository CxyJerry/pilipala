package com.jerry.pilipala.infrastructure.utils.unit;

public class GB extends MB {
    public GB(Integer count) {
        super(count);
    }

    @Override
    public Integer unit() {
        return super.unit() * 1000;
    }

    public static GB create(int count) {
        return new GB(count);
    }
}
