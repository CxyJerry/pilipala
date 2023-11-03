package com.jerry.pilipala.domain.vod.entity.mongo.vod;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Accessors(chain = true)
@Document("bvod")
public class BVod {
    @Id
    private String bvId;
    private String uid;
    private Boolean ready = false;
    private List<Long> cidList;
    private Long ctime = System.currentTimeMillis();
    private Long mtime = System.currentTimeMillis();
}
