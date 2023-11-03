package com.jerry.pilipala.domain.vod.entity.mongo.vod;

import com.jerry.pilipala.domain.vod.entity.mongo.media.Container;
import com.jerry.pilipala.domain.vod.entity.mongo.media.Audio;
import com.jerry.pilipala.domain.vod.entity.mongo.media.Video;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Accessors(chain = true)
@Document("vod")
public class Vod {
    @Id
    private Long cid;
    private String bvId;
    private String filename;
    private String ext;
    private Video video;
    private Audio audio;
    private Container container;
    private String extra;
    private Long ctime = System.currentTimeMillis();
    private Long mtime = System.currentTimeMillis();
}
