package com.jerry.pilipala.infrastructure.enums.video;

import com.jerry.pilipala.infrastructure.enums.MediaFormat;

public class VideoFormatEnum extends MediaFormat {
    public static final VideoFormatEnum mp4 = new VideoFormatEnum("mp4", "mp4");
    public static final VideoFormatEnum flv = new VideoFormatEnum("flv", "mp4");
    public static final VideoFormatEnum avi = new VideoFormatEnum("avi", "avi");
    public static final VideoFormatEnum mkv = new VideoFormatEnum("mkv", "mkv");
    public static final VideoFormatEnum webm = new VideoFormatEnum("webm", "webm");
    public static final VideoFormatEnum mov = new VideoFormatEnum("mov", "mov");
    public static final VideoFormatEnum wmv = new VideoFormatEnum("wmv", "wmv");
    public static final VideoFormatEnum DASH = new VideoFormatEnum("dash", "mpd");

    protected VideoFormatEnum(String value, String ext) {
        super(value, ext);
    }
}
