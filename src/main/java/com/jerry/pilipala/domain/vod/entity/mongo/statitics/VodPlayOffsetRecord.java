package com.jerry.pilipala.domain.vod.entity.mongo.statitics;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("vod_play_offset_record")
@Accessors(chain = true)
public class VodPlayOffsetRecord {
    @Id
    private ObjectId id;
    private String uid;
    private Long cid;
    private Integer time = 0;

    public static VodPlayOffsetRecord ZERO = new VodPlayOffsetRecord().setTime(0);
}
