package com.jerry.pilipala.domain.vod.entity.mongo.media;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Video {
    private Integer bitDepth;
    private Double bitRate;
    private String colorSpace;
    private String codecId;
    private Double displayAspectRatio;
    private Double duration;
    private String format;
    private String formatProfile;
    private Long frameCount;
    private Double frameRate;
    private String frameRateMode;
    private Integer height;
    private Integer width;
    private Double pixelAspectRatio;
    private String rotation;
    private String scanType;
    private String colourPrimaries;
    private String transferCharacteristics;
}
