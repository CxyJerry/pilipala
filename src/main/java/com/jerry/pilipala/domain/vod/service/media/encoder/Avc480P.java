package com.jerry.pilipala.domain.vod.service.media.encoder;

import com.jerry.pilipala.infrastructure.enums.FrameRateEnum;
import com.jerry.pilipala.infrastructure.enums.Qn;
import com.jerry.pilipala.infrastructure.enums.audio.AudioBitrateEnum;
import com.jerry.pilipala.infrastructure.enums.video.Resolution;
import com.jerry.pilipala.infrastructure.enums.video.VideoBitrateEnum;

public class Avc480P extends Encoder {
    public Avc480P() {
        super(Resolution._480P,
                FrameRateEnum._30.count(),
                VideoBitrateEnum._400K.value(),
                AudioBitrateEnum._32K.value());
    }

    @Override
    public Qn quality() {
        return Qn._480P;
    }
}
