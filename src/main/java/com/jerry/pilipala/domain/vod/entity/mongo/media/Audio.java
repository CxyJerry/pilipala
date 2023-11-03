package com.jerry.pilipala.domain.vod.entity.mongo.media;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Audio {
    private Double bitRate;
    private String bitRateMode;
    private String channelLayout;
    private String channelPositions;
    private Integer channels;
    private String codecId;
    private String compressionMode;
    private Double duration;
    private String format;
    private String formatAdditionalFeatures;
    private Long frameCount;
    private Integer samplesPerFrame;
    private Long samplingCount;
    private Double samplingRate;
}
