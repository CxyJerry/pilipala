package com.jerry.pilipala.domain.vod.entity.mongo.vod;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("vod_comment")
@Accessors(chain = true)
public class Comment {
    @Id
    private ObjectId id;
    private String parentId;
    private String uid;
    private String cid;
    private String content;
    private Integer childCount = 0;
    private Long ctime = System.currentTimeMillis();
    private Long mtime = System.currentTimeMillis();
}
