package com.jerry.pilipala.domain.vod.entity.neo4j;

import com.jerry.pilipala.domain.user.entity.neo4j.UserEntity;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Data
@Node("vod-info")
@Accessors(chain = true)
public class VodInfoEntity {
    @Id
    private Long cid;
    private String bvId;
    private String authorId;
    private String coverUrl;
    private String title;
    private String desc;
    private String gcType;
    private String partition;
    private String subPartition;
    private List<String> labels;
    private Integer viewCount = 0;
    private Integer likeCount = 0;
    private Integer barrageCount = 0;
    private Integer commentCount = 0;
    private Integer coinCount = 0;
    private Integer collectCount = 0;
    private Integer shareCount = 0;
    private Long ctime;

    @Relationship(type = "CreatedBy")
    private UserEntity author;
}
