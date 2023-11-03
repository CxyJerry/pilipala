package com.jerry.pilipala.domain.vod.service.media.profiles;

import com.jerry.pilipala.infrastructure.enums.video.VideoFormatEnum;
import com.jerry.pilipala.domain.vod.service.media.encoder.AvcPreview;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class _Preview extends Profile {
    public _Preview(String filename) {
        super(new AvcPreview(),
                VideoFormatEnum.DASH,
                filename);
        this.setEnableAudio(false);
        this.setDuration(30);
    }
}
