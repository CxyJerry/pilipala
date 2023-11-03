package com.jerry.pilipala.domain.vod.service.media;

import com.google.common.collect.Lists;

import com.jerry.pilipala.domain.vod.entity.mongo.vod.Vod;
import com.jerry.pilipala.domain.vod.service.media.profiles.*;
import com.jerry.pilipala.infrastructure.enums.video.Resolution;

import com.jerry.pilipala.domain.vod.entity.mongo.media.Video;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UGCSchema implements Schema {

    @Override
    public List<Profile> selectAvProfiles(Vod vod) {
        Video video = vod.getVideo();
        Resolution resolution = Resolution.create(video.getWidth(), video.getHeight());

        if (resolution.shorter() < Resolution._360P.shorter()) {
            return Lists.newArrayList(
                    new _Preview(vod.getFilename()),
                    new _360P(vod.getFilename())
            );
        } else if (resolution.shorter() < Resolution._480P.shorter()) {
            return Lists.newArrayList(
                    new _Preview(vod.getFilename()),
                    new _360P(vod.getFilename()),
                    new _480P(vod.getFilename())
            );
        } else if (resolution.shorter() < Resolution._720P.shorter()) {
            return Lists.newArrayList(
                    new _Preview(vod.getFilename()),
                    new _360P(vod.getFilename()),
                    new _480P(vod.getFilename()),
                    new _720P(vod.getFilename())
            );
        } else {
            return Lists.newArrayList(
                    new _Preview(vod.getFilename()),
                    new _360P(vod.getFilename()),
                    new _480P(vod.getFilename()),
                    new _720P(vod.getFilename()),
                    new _1080P(vod.getFilename())
            );
        }
    }


}
