package com.jerry.pilipala.domain.vod.service.media.encoder;

import com.jerry.pilipala.infrastructure.enums.FrameRateEnum;
import com.jerry.pilipala.infrastructure.enums.Qn;
import com.jerry.pilipala.infrastructure.enums.audio.AudioBitrateEnum;
import com.jerry.pilipala.infrastructure.enums.video.Resolution;
import com.jerry.pilipala.infrastructure.enums.video.VideoBitrateEnum;

public class Avc1080P extends Encoder {
    public Avc1080P() {
        super(Resolution._1080P,
                FrameRateEnum._30.count(),
                VideoBitrateEnum._3M.value(),
                AudioBitrateEnum._128K.value());
    }

    @Override
    public Qn quality() {
        return Qn._1080P;
    }
}
