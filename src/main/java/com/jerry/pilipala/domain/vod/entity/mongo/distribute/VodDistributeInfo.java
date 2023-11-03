package com.jerry.pilipala.domain.vod.entity.mongo.distribute;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Document("vod_distribute_info")
@Accessors(chain = true)
public class VodDistributeInfo {
    @Id
    private Long cid;
    private String filename;
    private Boolean ready;
    private Map<Integer, Quality> qualityMap;
}
