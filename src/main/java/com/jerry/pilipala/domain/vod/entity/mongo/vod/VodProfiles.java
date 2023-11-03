package com.jerry.pilipala.domain.vod.entity.mongo.vod;

import com.jerry.pilipala.domain.vod.service.media.profiles.Profile;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document("vod_profiles")
@Accessors(chain = true)
public class VodProfiles {
    @Id
    private Long cid;
    private Boolean completed;
    private List<Profile> profiles;
    private Long ctime = System.currentTimeMillis();
    private Long mtime = System.currentTimeMillis();

}
