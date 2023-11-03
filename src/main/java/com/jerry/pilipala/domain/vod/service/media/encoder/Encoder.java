package com.jerry.pilipala.domain.vod.service.media.encoder;

import com.jerry.pilipala.domain.vod.entity.mongo.vod.Vod;
import com.jerry.pilipala.infrastructure.enums.Qn;
import com.jerry.pilipala.infrastructure.enums.video.Resolution;
import com.jerry.pilipala.domain.vod.entity.mongo.media.Video;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Data
@NoArgsConstructor
@Accessors(chain = true)
public abstract class Encoder {
    private Resolution resolution;
    private int frameRate = 0;
    private int videoBitrate = 0;
    private int audioBitrate = 0;
    private Resolution targetResolution;
    private int targetFrameRate;
    private int targetVideoBitrate;
    private int targetAudioBitrate;
    private String videoCodec = "libx264";
    private String audioCodec = "aac";

    public Encoder(Resolution targetResolution, int targetFrameRate, int targetVideoBitrate, int targetAudioBitrate) {
        this.targetResolution = targetResolution;
        this.targetFrameRate = targetFrameRate;
        this.targetVideoBitrate = targetVideoBitrate;
        this.targetAudioBitrate = targetAudioBitrate;
    }

    private void fitResolution(Vod vod) {
        Video video = vod.getVideo();
        Resolution videoResolution = Resolution.create(video.getWidth(), video.getHeight());
        if (videoResolution.longer() != targetResolution.longer()) {
            double scaleFactor = targetResolution.longer() * 1.0 / videoResolution.longer();
            log.info("scaling {} as required: {}/{}",
                    scaleFactor > 1 ? "up" : "down",
                    targetResolution.longer(),
                    videoResolution.longer()
            );
            videoResolution = videoResolution.scale(scaleFactor);
        }
        if (!videoResolution.valid()) {
            log.info("scaling down to fix incorrect dimensions");
            videoResolution = videoResolution.correct();
        }

        if (StringUtils.isNotBlank(video.getRotation())) {
            try {
                if (Double.parseDouble(video.getRotation()) != 0) {
                    videoResolution = videoResolution.rotate();
                }
            } catch (Exception e) {
                log.info("rotate resolution fix error,", e);
            }
        }
        this.resolution = videoResolution;
        log.info("resolution fit: {}x{} -> {}x{}",
                video.getWidth(),
                video.getHeight(),
                this.resolution.getWidth(),
                this.resolution.getHeight());

    }

    private void fitFrameRate(Vod vod) {
        Video video = vod.getVideo();
        this.frameRate = (int) Math.min(video.getFrameRate(), targetFrameRate);
        log.info("frame rate fit: {} -> {}", video.getFrameRate(), frameRate);
    }

    private void fitBitrate(Vod vod) {
        this.videoBitrate = (int) Math.min(vod.getVideo().getBitRate() * 95 / 100, targetVideoBitrate);
        log.info("video bitrate fit: {} -> {}", vod.getVideo().getBitRate(), videoBitrate);
        this.audioBitrate = (int) Math.min(vod.getAudio().getBitRate(), targetAudioBitrate);
        log.info("audio bitrate fit: {} -> {}", vod.getAudio().getBitRate(), audioBitrate);
    }

    public void fitInput(Vod vod) {
        fitResolution(vod);
        fitFrameRate(vod);
        fitBitrate(vod);
    }

    public abstract Qn quality();

}
