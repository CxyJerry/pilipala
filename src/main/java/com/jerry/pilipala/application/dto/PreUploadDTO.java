package com.jerry.pilipala.application.dto;

import com.jerry.pilipala.domain.vod.entity.mongo.media.Audio;
import com.jerry.pilipala.domain.vod.entity.mongo.media.Container;
import com.jerry.pilipala.domain.vod.entity.mongo.media.Video;
import lombok.Data;

@Data
public class PreUploadDTO {
    private String bvId;
    private Video video;
    private Audio audio;
    private Container container;
    private String extra;
}
