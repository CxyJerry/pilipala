package com.jerry.pilipala.domain.vod.entity.mongo.event;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Accessors(chain = true)
@Document("vod_handle_record")
public class VodHandleActionRecord {
    @Id
    private ObjectId id;
    @Indexed
    private Long cid;
    @Indexed
    private String filename;
    private String action;
    private String status;
    private Long ctime = System.currentTimeMillis();
    private Long mtime = System.currentTimeMillis();
}
