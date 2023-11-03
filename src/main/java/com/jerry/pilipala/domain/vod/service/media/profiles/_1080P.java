package com.jerry.pilipala.domain.vod.service.media.profiles;

import com.jerry.pilipala.domain.vod.service.media.encoder.Avc1080P;
import com.jerry.pilipala.infrastructure.enums.video.VideoFormatEnum;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class _1080P extends Profile {
    public _1080P(String filename) {
        super(new Avc1080P(),
                VideoFormatEnum.DASH,
                filename);
    }
}
