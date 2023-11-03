package com.jerry.pilipala.domain.vod.service.media.profiles;

import com.jerry.pilipala.domain.vod.service.media.encoder.Avc720P;
import com.jerry.pilipala.infrastructure.enums.video.VideoFormatEnum;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class _720P extends Profile {
    public _720P(String filename) {
        super(new Avc720P(),
                VideoFormatEnum.DASH,
                filename);
    }
}
