package com.jerry.pilipala.domain.vod.service.media.encoder;

import com.jerry.pilipala.infrastructure.enums.FrameRateEnum;
import com.jerry.pilipala.infrastructure.enums.Qn;
import com.jerry.pilipala.infrastructure.enums.audio.AudioBitrateEnum;
import com.jerry.pilipala.infrastructure.enums.video.Resolution;
import com.jerry.pilipala.infrastructure.enums.video.VideoBitrateEnum;


public class Avc360P extends Encoder {
    public Avc360P() {
        super(Resolution._360P,
                FrameRateEnum._30.count(),
                VideoBitrateEnum._400K.value(),
                AudioBitrateEnum._32K.value());
    }

    @Override
    public Qn quality() {
        return Qn._360P;
    }
}
