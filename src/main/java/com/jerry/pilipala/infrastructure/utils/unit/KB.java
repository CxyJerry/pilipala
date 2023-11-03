package com.jerry.pilipala.infrastructure.utils.unit;

public class KB implements Unit<Integer> {
    private final Integer count;

    public KB(Integer count) {
        this.count = count;
    }


    @Override
    public Integer value() {
        return count * unit();
    }

    @Override
    public Integer unit() {
        return 1000;
    }

    public static KB create(int count) {
        return new KB(count);
    }
}
