package com.jerry.pilipala.infrastructure.enums.audio;

import com.jerry.pilipala.infrastructure.enums.MediaFormat;
import lombok.Getter;

@Getter
public class AudioFormat extends MediaFormat {
    public static final AudioFormat aac = new AudioFormat("aac", "aac");
    public static final AudioFormat flac = new AudioFormat("flac", "flac");
    public static final AudioFormat mp3 = new AudioFormat("mp3", "mp3");
    public static final AudioFormat wav = new AudioFormat("wav", "wav");
    public static final AudioFormat m4a = new AudioFormat("m4a", "m4a");

    protected AudioFormat(String value, String ext) {
        super(value, ext);
    }
}
