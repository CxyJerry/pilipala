package com.jerry.pilipala.domain.vod.entity.mongo.statitics;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Accessors(chain = true)
@Document("vod_play_time_record")
public class VodPlayTimeRecord {
    @Id
    private ObjectId id;
    private String tel;
    private Long cid;
    private Integer time;
}
