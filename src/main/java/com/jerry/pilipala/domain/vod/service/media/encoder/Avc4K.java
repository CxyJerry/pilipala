package com.jerry.pilipala.domain.vod.service.media.encoder;

import com.jerry.pilipala.infrastructure.enums.FrameRateEnum;
import com.jerry.pilipala.infrastructure.enums.Qn;
import com.jerry.pilipala.infrastructure.enums.audio.AudioBitrateEnum;
import com.jerry.pilipala.infrastructure.enums.video.Resolution;
import com.jerry.pilipala.infrastructure.enums.video.VideoBitrateEnum;

public class Avc4K extends Encoder {
    public Avc4K() {
        super(Resolution._4K,
                FrameRateEnum._30.count(),
                VideoBitrateEnum._20M.value(),
                AudioBitrateEnum._32K.value());
    }

    @Override
    public Qn quality() {
        return Qn._4K;
    }
}
