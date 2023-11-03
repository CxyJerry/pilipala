package com.jerry.pilipala.domain.vod.service.media.profiles;

import com.jerry.pilipala.domain.vod.service.media.encoder.Encoder;
import com.jerry.pilipala.infrastructure.enums.MediaFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public abstract class Profile {
    private boolean enableVideo = true;
    private boolean enableAudio = true;
    private long duration = 0L;
    private Encoder encoder;
    private MediaFormat format;
    private String saveTo;

    public Profile(Encoder encoder, MediaFormat format, String filename) {
        this.encoder = encoder;
        this.format = format;
        this.saveTo = "%s/%s".formatted(filename, encoder.quality().getQn());
    }


}
