package com.jerry.pilipala.domain.vod.service.media.profiles;

import com.jerry.pilipala.domain.vod.service.media.encoder.Avc4K;
import com.jerry.pilipala.infrastructure.enums.video.VideoFormatEnum;

public class _4K extends Profile {
    public _4K(String filename) {
        super(new Avc4K(),
                VideoFormatEnum.DASH,
                filename);
    }
}
