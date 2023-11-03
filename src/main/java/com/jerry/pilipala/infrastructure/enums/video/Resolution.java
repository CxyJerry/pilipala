package com.jerry.pilipala.infrastructure.enums.video;

import lombok.Getter;

@Getter
public class Resolution {

    public static final Resolution _PREVIEW = new Resolution(640, 360);
    public static final Resolution _360P = new Resolution(640, 360);
    public static final Resolution _480P = new Resolution(854, 480);
    public static final Resolution _720P = new Resolution(1280, 720);
    public static final Resolution _1080P = new Resolution(1920, 1080);
    public static final Resolution _2K = new Resolution(2560, 1440);
    public static final Resolution _4K = new Resolution(4096, 2160);
    public static final Resolution _8K = new Resolution(8192, 4320);

    private final int width;
    private final int height;

    Resolution(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Resolution scale(double factor) {
        int width = ((int) (this.width * factor)) & (~1);
        int height = ((int) (this.height * factor)) & (~1);
        return new Resolution(width, height);
    }

    /**
     * 精确缩放
     *
     * @param factor 缩放比例
     * @return 缩放后的清晰度
     */
    public Resolution preciseScale(double factor) {
        int width = ((int) (this.width * factor));
        int height = ((int) (this.height * factor));
        return new Resolution(width, height);
    }

    /**
     * 修正为合法的清晰度
     *
     * @return 修正后的清晰度
     */
    public Resolution correct() {
        int width = this.width & (~1);
        int height = this.height & (~1);
        return new Resolution(width, height);
    }

    public int shorter() {
        return Math.min(width, height);
    }

    public int longer() {
        return Math.max(width, height);
    }

    public boolean valid() {
        return (width & 0x01) == 0 && (height & 0x01) == 0;
    }

    public int pixels() {
        return width * height;
    }

    /**
     * 宽高比
     *
     * @return ratio
     */
    public double aspectRatio() {
        return longer() * 1.0 / shorter();
    }

    public Resolution rotate() {
        return new Resolution(height, width);
    }

    public static Resolution create(int width, int height) {
        return new Resolution(width, height);
    }
}
