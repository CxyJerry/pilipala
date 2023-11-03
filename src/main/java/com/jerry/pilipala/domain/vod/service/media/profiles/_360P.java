package com.jerry.pilipala.domain.vod.service.media.profiles;

import com.jerry.pilipala.domain.vod.service.media.encoder.Avc360P;
import com.jerry.pilipala.infrastructure.enums.video.VideoFormatEnum;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class _360P extends Profile {
    public _360P(String filename) {
        super(new Avc360P(),
                VideoFormatEnum.DASH,
                filename);
    }
}
