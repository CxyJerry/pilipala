package com.jerry.pilipala.domain.vod.entity.mongo.vod;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Accessors(chain = true)
@Document("vod_danmaku")
public class Danmaku {
    @Id
    private ObjectId id;
    private Long cid;
    private String uid;
    private String sender;
    private Double time;
    private String text;
    private Integer color;
    private Integer visible = 0;
    private Long ctime = System.currentTimeMillis();
    private Long mtime = System.currentTimeMillis();
}
