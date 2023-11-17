package com.jerry.pilipala.domain.vod.entity.mongo.vod;

import com.jerry.pilipala.infrastructure.enums.VodStatusEnum;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Accessors(chain = true)
@Document("vod_info")
public class VodInfo {
    @Id
    private Long cid;
    private String bvId;
    private String uid;
    private VodStatusEnum status;
    private String coverUrl;
    @TextIndexed
    private String title;
    private String gcType;
    private String partition;
    private String subPartition;
    private List<String> labels;
    private String desc;
    private Double duration;
    private Long ctime = System.currentTimeMillis();
    private Long mtime = System.currentTimeMillis();
}
