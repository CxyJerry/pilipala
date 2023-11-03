package com.jerry.pilipala.domain.vod.service.media.profiles;

import com.jerry.pilipala.domain.vod.service.media.encoder.Avc480P;
import com.jerry.pilipala.infrastructure.enums.video.VideoFormatEnum;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class _480P extends Profile {
    public _480P(String filename) {
        super(new Avc480P(),
                VideoFormatEnum.DASH,
                filename);
    }
}
