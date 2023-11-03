package com.jerry.pilipala.domain.vod.entity.mongo.media;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Container {
    private Double duration;
    private Long fileSize;
    private String format;
    private String formatProfile;
    private Integer frameCount;
    private Double frameRate;
    private Long overAllBitRate;
}
