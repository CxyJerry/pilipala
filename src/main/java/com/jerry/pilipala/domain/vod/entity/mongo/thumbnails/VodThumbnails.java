package com.jerry.pilipala.domain.vod.entity.mongo.thumbnails;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Accessors(chain = true)
@Document("vod_thumbnails")
public class VodThumbnails {
    @Id
    private Long cid;
    private List<Thumbnails> thumbnails;
    private Long ctime = System.currentTimeMillis();
    private Long mtime = System.currentTimeMillis();
}
