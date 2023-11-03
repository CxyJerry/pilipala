package com.jerry.pilipala.domain.vod.service.media;


import com.jerry.pilipala.domain.vod.service.media.profiles.Profile;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.Vod;

import java.util.List;

public interface Schema {
    /**
     * 选择恰当的 profiles
     *
     * @param vod 视频元信息
     * @return profiles
     */
    List<Profile> selectAvProfiles(Vod vod);

}
