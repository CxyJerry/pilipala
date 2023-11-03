package com.jerry.pilipala.infrastructure.enums;

import lombok.Getter;

@Getter
public abstract class MediaFormat {

    protected final String value;
    protected final String ext;

    protected MediaFormat(String value, String ext) {
        this.value = value;
        this.ext = ext;
    }

}
